from fileinput import close
import socket
import sys
import time
import random
import json

file = open("files/devices.json")
dic = json.load(file)

class Device:
    def __init__(self,host,port):
        self.host = host
        self.port = port
        self.socket = socket.socket(socket.AF_INET,socket.SOCK_STREAM)

    def sig_int_handler(self,signal,frame):
        pass

    def connect(self):
        self.socket.connect((self.host, self.port))
    
    def send(self,msg):
        self.socket.sendall(msg.encode('utf-8'))

    def recv(self,bytes):
        return self.socket.recv(bytes).decode('utf-8')

    def close(self):
        self.socket.close()

def main():
    args = sys.argv[1:]

    if len(args)==1 and args[0]=="-h":
        print("python3 device.py ID PASSWD TYPE IP PORT")
    elif len(args)==5:
        #HOST = args[3]
        #PORT = int(args[4])

        HOST = "localhost" # test only
        PORT = 1234        # test only

        if(args[2] in dic):
            device = Device(HOST,PORT)
            device.connect()
            
            msg = "login " + args[0] + " " + args[1] + " " + args[2] + "\n"
            try:
                device.send(msg)

                resp = device.recv(1024)
                if "success" in resp.lower():
                    print("success!!!")
                else:
                    print(resp)

                if resp :
                    while(True):
                        time.sleep(2)
                        event = random.choice(dic[args[2]])
                        msg = "event " + event + "\n"
                        device.send(msg)
                else:
                    print("Login failed")
            finally:
                device.close()
    
        else:
            print("Wrong type")
    else:
        print("Wrong arguments")


main()    
