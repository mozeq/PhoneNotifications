# file: rfcomm-server.py
# auth: Albert Huang <albert@csail.mit.edu>
# desc: simple demonstration of a server application that uses RFCOMM sockets
#
# $Id: rfcomm-server.py 518 2007-08-10 07:20:07Z albert $

from bluetooth import *
import struct
import simplejson





from gi.repository import Notify
Notify.init ("com.moskovcak.pns")

def notify_call(**kwargs):
    CallNotify = None
    state = kwargs.get("state","UNKNOWN")
    if (state == "RINGING"):
        CallNotify = Notify.Notification.new ("Incoming call from", "{0} ({1})".format(kwargs["callerId"], kwargs["contactName"]),"dialog-information")
    else:
        CallNotify = Notify.Notification.new("Call has ended","","dialog-information")

    CallNotify.show()

def notify_sms(**kwargs):
    SMSNotification=Notify.Notification.new ("New sms from {0} ({1})".format(kwargs["callerId"], kwargs["contactName"]), kwargs["message"], "dialog-information")
    SMSNotification.show ()

def hdne(**kwargs):
    print "Handler does not exist!"
    print kwargs

vtable = {
    "incomingCall":notify_call,
    "newSMS": notify_sms
}

server_sock=BluetoothSocket( RFCOMM )
server_sock.bind(("",PORT_ANY))
server_sock.listen(1)
port = server_sock.getsockname()[1]

#uuid = "00001101-0000-1000-8000-00805f9b34fb"
uuid = "1e0ca4ea-299d-4335-93eb-27fcfe7fa848"
advertise_service( server_sock, "SampleServer",
                   service_id = uuid,
                   service_classes = [ uuid, SERIAL_PORT_CLASS ],
                   profiles = [ SERIAL_PORT_PROFILE ],
#                   protocols = [ OBEX_UUID ]
                    )

while True: # when clients disconnects setup the server and wait for new connection

    print "Waiting for connection on RFCOMM channel %d" % port

    client_sock, client_info = server_sock.accept()
    print "Accepted connection from ", client_info

    try:
        while True:
            read = client_sock.recv(4)
            data_size = struct.unpack("!i", read)[0]

            print "data size: ", data_size

            data = None
            while data is None or len(data) < data_size:
                data = client_sock.recv(data_size)
                jsonObj = simplejson.loads(data)
                handler = vtable.get(jsonObj.get("methodName", "none"), hdne)
                handler(**jsonObj.get("args",{}))
                print "received [%s]" % data
    except IOError, ex:
        print ex
        pass

    print "disconnected"
    client_sock.close()

server_sock.close()
