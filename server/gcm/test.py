import unittest
from gcm import *
import json
from mock import MagicMock
import time


# Helper method to return a different value for each call.
def create_side_effect(returns):
    def side_effect(*args, **kwargs):
        result = returns.pop(0)
        if isinstance(result, Exception):
            raise result
        return result
    return side_effect


class GCMTest(unittest.TestCase):

    def setUp(self):
        self.gcm = GCM('123api')
        self.data = {
            'param1': '1',
            'param2': '2'
        }
        self.response = {
            'results': [
                {'error': 'InvalidRegistration'},
                {'error': 'NotRegistered'},
                {'message_id': '54749687859', 'registration_id': '6969'},
                {'message_id': '5456453453'},
                {'error': 'NotRegistered'},
                {'message_id': '123456778', 'registration_id': '07645'},
            ]
        }
        self.mock_response_1 = {
            'results': [
                {'error': 'Unavailable'},
                {'error': 'Unavailable'},
            ]
        }
        self.mock_response_2 = {
            'results': [
                {'error': 'Unavailable'},
                {'message_id': '1234'}
            ]
        }
        self.mock_response_3 = {
            'results': [
                {'message_id': '5678'},
                {'message_id': '1234'}
            ]
        }
        time.sleep = MagicMock()

    def test_construct_payload(self):
        res = self.gcm.construct_payload(
            registration_ids=['1', '2'], data=self.data, collapse_key='foo',
            delay_while_idle=True, time_to_live=3600, is_json=True
        )
        payload = json.loads(res)
        for arg in ['registration_ids', 'data', 'collapse_key', 'delay_while_idle', 'time_to_live']:
            self.assertIn(arg, payload)

    def test_require_collapse_key(self):
        with self.assertRaises(GCMNoCollapseKeyException):
            self.gcm.construct_payload(registration_ids='1234', data=self.data, time_to_live=3600)

    def test_json_payload(self):
        reg_ids = ['12', '145', '56']
        json_payload = self.gcm.construct_payload(registration_ids=reg_ids, data=self.data)
        payload = json.loads(json_payload)

        self.assertIn('registration_ids', payload)
        self.assertEqual(payload['data'], self.data)
        self.assertEqual(payload['registration_ids'], reg_ids)

    def test_plaintext_payload(self):
        result = self.gcm.construct_payload(registration_ids='1234', data=self.data, is_json=False)

        self.assertIn('registration_id', result)
        self.assertIn('data.param1', result)
        self.assertIn('data.param2', result)

    def test_limit_reg_ids(self):
        reg_ids = range(1003)
        self.assertTrue(len(reg_ids) > 1000)
        with self.assertRaises(GCMTooManyRegIdsException):
            self.gcm.json_request(registration_ids=reg_ids, data=self.data)

    def test_missing_reg_id(self):
        with self.assertRaises(GCMMissingRegistrationException):
            self.gcm.json_request(registration_ids=[], data=self.data)

        with self.assertRaises(GCMMissingRegistrationException):
            self.gcm.plaintext_request(registration_id=None, data=self.data)

    def test_invalid_ttl(self):
        with self.assertRaises(GCMInvalidTtlException):
            self.gcm.construct_payload(
                registration_ids='1234', data=self.data, is_json=False, time_to_live=5000000
            )

        with self.assertRaises(GCMInvalidTtlException):
            self.gcm.construct_payload(
                registration_ids='1234', data=self.data, is_json=False, time_to_live=-10
            )

    def test_group_response(self):
        ids = ['123', '345', '678', '999', '1919', '5443']
        error_group = group_response(self.response, ids, 'error')
        self.assertEqual(error_group['NotRegistered'], ['345', '1919'])
        self.assertEqual(error_group['InvalidRegistration'], ['123'])

        canonical_group = group_response(self.response, ids, 'registration_id')
        self.assertEqual(canonical_group['678'], '6969')
        self.assertEqual(canonical_group['5443'], '07645')

    def test_group_response_no_error(self):
        ids = ['123', '345', '678']
        response = {
            'results': [
                {'message_id': '346547676'},
                {'message_id': '54749687859'},
                {'message_id': '5456453453'},
            ]
        }
        error_group = group_response(response, ids, 'error')
        canonical_group = group_response(response, ids, 'registration_id')
        self.assertEqual(error_group, None)
        self.assertEqual(canonical_group, None)

    def test_handle_json_response(self):
        ids = ['123', '345', '678', '999', '1919', '5443']
        res = self.gcm.handle_json_response(self.response, ids)

        self.assertIn('errors', res)
        self.assertIn('NotRegistered', res['errors'])
        self.assertIn('canonical', res)
        self.assertIn('678', res['canonical'])

    def test_handle_json_response_no_error(self):
        ids = ['123', '345', '678']
        response = {
            'results': [
                {'message_id': '346547676'},
                {'message_id': '54749687859'},
                {'message_id': '5456453453'},
            ]
        }
        res = self.gcm.handle_json_response(response, ids)

        self.assertNotIn('errors', res)
        self.assertNotIn('canonical', res)

    def test_handle_plaintext_response(self):
        response = 'Error=NotRegistered'
        with self.assertRaises(GCMNotRegisteredException):
            self.gcm.handle_plaintext_response(response)

        response = 'id=23436576'
        res = self.gcm.handle_plaintext_response(response)
        self.assertIsNone(res)

        response = 'id=23436576\nregistration_id=3456'
        res = self.gcm.handle_plaintext_response(response)
        self.assertEqual(res, '3456')

    def test_retry_plaintext_request_ok(self):
        returns = [GCMUnavailableException(), GCMUnavailableException(), 'id=123456789']

        self.gcm.make_request = MagicMock(side_effect=create_side_effect(returns))
        res = self.gcm.plaintext_request(registration_id='1234', data=self.data)

        self.assertIsNone(res)
        self.assertEqual(self.gcm.make_request.call_count, 3)

    def test_retry_plaintext_request_fail(self):
        returns = [GCMUnavailableException(), GCMUnavailableException(), GCMUnavailableException()]

        self.gcm.make_request = MagicMock(side_effect=create_side_effect(returns))
        with self.assertRaises(IOError):
            self.gcm.plaintext_request(registration_id='1234', data=self.data, retries=2)

        self.assertEqual(self.gcm.make_request.call_count, 2)

    def test_retry_json_request_ok(self):
        returns = [self.mock_response_1, self.mock_response_2, self.mock_response_3]

        self.gcm.make_request = MagicMock(side_effect=create_side_effect(returns))
        res = self.gcm.json_request(registration_ids=['1', '2'], data=self.data)

        self.assertEqual(self.gcm.make_request.call_count, 3)
        self.assertNotIn('errors', res)

    def test_retry_json_request_fail(self):
        returns = [self.mock_response_1, self.mock_response_2, self.mock_response_3]

        self.gcm.make_request = MagicMock(side_effect=create_side_effect(returns))
        res = self.gcm.json_request(registration_ids=['1', '2'], data=self.data, retries=2)

        self.assertEqual(self.gcm.make_request.call_count, 2)
        self.assertIn('Unavailable', res['errors'])
        self.assertEqual(res['errors']['Unavailable'][0], '1')

    def test_retry_exponential_backoff(self):
        returns = [GCMUnavailableException(), GCMUnavailableException(), 'id=123456789']

        self.gcm.make_request = MagicMock(side_effect=create_side_effect(returns))
        self.gcm.plaintext_request(registration_id='1234', data=self.data)

        # time.sleep is actually mock object.
        self.assertEqual(time.sleep.call_count, 2)
        backoff = self.gcm.BACKOFF_INITIAL_DELAY
        for arg in time.sleep.call_args_list:
            sleep_time = int(arg[0][0] * 1000)
            self.assertTrue(backoff / 2 <= sleep_time <= backoff * 3 / 2)
            if 2 * backoff < self.gcm.MAX_BACKOFF_DELAY:
                backoff *= 2

if __name__ == '__main__':
    unittest.main()
