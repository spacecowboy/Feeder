from __future__ import print_function, division
from gcm import GCM

from google.appengine.ext import ndb

gcm = GCM('AIzaSyDH8p6RpggRjDp1PcGewmYsLGsoZC2kBJ0')

# ACTIONS
ACTION_SYNC = "sync"


class GCMRegIdModel(ndb.Model):
    regid = ndb.StringProperty(required=True)
    userid = ndb.UserProperty(required=True)


def send_sync_notices(userids):
    '''
    Send a notice for the users' devices to do a sync.
    '''
    for userid in userids:
        # Get devices
        reg_ids = []
        query = GCMRegIdModel.query(GCMRegIdModel.userid == userid)

        for reg_model in query:
            reg_ids.append(reg_model.regid)

        if len(reg_ids) > 0:
            print("Sending sync notice to:", userid, reg_ids[0])
            _send(userid, reg_ids, dict(action=ACTION_SYNC))


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
            print("Got error in GCM:", error)
            # Check for errors and act accordingly
            if (error == 'NotRegistered' or
                error == 'InvalidRegistration'):
                # Remove reg_ids from database
                for regid in reg_ids:
                    _remove_regid(regid)
