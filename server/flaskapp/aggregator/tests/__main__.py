import sys
import pytest


if __name__ == '__main__':
    # Call tests and exit with correct code
    sys.exit(pytest.main(["--pyargs", "aggregator.tests"] + sys.argv[1:]))
