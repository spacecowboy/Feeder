from __future__ import print_function, division
from threading import Thread
from functools import wraps
from gcm import GCM

from google.appengine.ext import ndb

gcm = GCM('AIzaSyALWhrcJKnGWVjOisD_WjmP2SOiOIJA9F4')

class GCMRegIdModel(ndb.Model):
    regid = ndb.StringProperty(required=True)
    userid = ndb.UserProperty(required=True)

def to_dict(link):
    return dict(sha=link.sha,
                url=link.url,
                timestamp=link.timestamp.isoformat(sep=" "),
                deleted=link.deleted)


def send_link(link, excludeid=None):
    '''Transmits the link specified by the sha to the users devices.

    Does not run in a separate thread because App-Engine did not
    seem to support that.
    '''
    # Get devices
    reg_ids = []
    query = GCMRegIdModel.query(GCMRegIdModel.userid == link.userid)

    for reg_model in query:
        reg_ids.append(reg_model.regid)

    # Dont send to origin device, if specified
    try:
        reg_ids.remove(excludeid)
    except ValueError:
        pass # not in list, or None

    if len(reg_ids) < 1:
        return

    _send(link.userid, reg_ids, to_dict(link))


def _remove_regid(regid):
    ndb.Key(GCMRegIdModel, regid).delete()


def _replace_regid(userid, oldid, newid):
    _remove_regid(oldid)
    device = GCMRegIdModel(key=ndb.Key(GCMRegIdModel, newid),
                           regid=newid,
                           userid=userid)
    device.put()


def _send(userid, rids, data):
    '''Send the data using GCM'''
    response = gcm.json_request(registration_ids=rids,
                                data=data,
                                delay_while_idle=True)

    # A device has switched registration id
    if 'canonical' in response:
        for reg_id, canonical_id in response['canonical'].items():
            # Repace reg_id with canonical_id in your database
            _replace_regid(userid, reg_id, canonical_id)

    # Handling errors
    if 'errors' in response:
        for error, reg_ids in response['errors'].items():
            # Check for errors and act accordingly
            if (error == 'NotRegistered' or
                error == 'InvalidRegistration'):
                # Remove reg_ids from database
                for regid in reg_ids:
                    _remove_regid(regid)
