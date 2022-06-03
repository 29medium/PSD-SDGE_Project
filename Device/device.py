import socket
import sys
import time
import random

dic = {
    "car" : ["accelerate", "brake", "hand-brake on", "hand-brake off", "turn-left", "turn-right"],
    "light" : ["on", "off"],
    "drone" : ["up", "down"]
}

def main():
    args = sys.argv[1:]

    if len(args)==1 and args[0]=="-h":
        print("python3 device.py ID PASSWD TYPE IP PORT")
    elif len(args)==5:
        #HOST = args[3]
        #PORT = int(args[4])

        HOST = "localhost"
        PORT = 1234

        if(args[2] in dic):
            s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            s.connect((HOST, PORT))
            
            msg = "login " + args[0] + " " + args[1] + " " + args[2] + "\n"
            s.sendall(msg.encode('utf-8'))

            resp = s.recv(1024)
            print(resp)

            if resp :
                while(True):
                    time.sleep(2)
                    event = random.choice(dic[args[2]])
                    msg = "event " + event + "\n"
                    s.sendall(msg.encode("utf8"))
            else:
                print("Login failed")
        else:
            print("Wrong type")
    else:
        print("Wrong arguments")


main()    