from yaml import load


def read_config(filename):
    with open(filename, 'r') as FILE:
        return load("".join(FILE.readlines()))


if __name__ == '__main__':
    conf = read_config('config-sample.yaml')

    print("DB", conf.get('DATABASE', 'default'))
    print("GGL", conf.get('ALLOW_GOOGLE', 'default'))
    print("USR", conf.get('ALLOW_USERPASS', 'default'))
    print("Users:", conf.get('Users', []))
