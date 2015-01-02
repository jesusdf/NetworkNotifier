#!/usr/bin/python
import socket
import lxml.etree
import lxml.builder
import hashlib
import os, stat
import sys
import Tkinter, tkSimpleDialog

VERSION = "1.0"
UDP_IP = "224.0.0.1"
UDP_IPV6 = "FF01:0:0:0:0:0:0:1"
UDP_PORT = 7415
ENCODING = 'utf-8'
LOOPBACK = "127."

#Initialization
try:
    # get IP
    LOCAL_IP = [ip for ip in socket.gethostbyname_ex(socket.gethostname())[2] if not ip.startswith(LOOPBACK)][:1]
    # or HostName
    textSource = socket.gethostname()
    mode = os.fstat(0).st_mode
    if stat.S_ISFIFO(mode):
        textMessage = raw_input().decode(ENCODING)
    else:
        if sys.stdin.isatty():
        	textMessage = raw_input().decode(ENCODING)
        else:
            root = Tkinter.Tk()
            root.withdraw()
	    textMessage = tkSimpleDialog.askstring(title='Network Notifier Client', prompt='Message to send:')
	    if textMessage is None:
	        textMessage = ''
except:
    print "Initialization failed! :("
    raise

#print "Host IP: ", LOCAL_IP
#print "UDP target IP: ", UDP_IP
#print "UDP target port: ", UDP_PORT
#print "Source: ", textSource
#print "Message: ", textMessage

# Prepare the XML version of the message
try:
    E = lxml.builder.ElementMaker()
    NETWORKNOTIFIER = E.NetworkNotifier
    NOTIFICATION = E.Notification
    HASH = E.Hash
    SOURCE = E.Source
    MESSAGE = E.Message

    xmlMessage = NETWORKNOTIFIER (
        NOTIFICATION (
            SOURCE(textSource),
            MESSAGE(textMessage),
            HASH (
                hashlib.sha256("%s-%s" %(textSource, textMessage.encode(ENCODING))).hexdigest()
            )
        ),
        version="%s" %(VERSION)
    )
except:
    print "Failed building XML message:"
    raise

xmlFinalMessage = lxml.etree.tostring(xmlMessage, pretty_print=True)

#print xmlFinalMessage

try:
    sock = socket.socket(socket.AF_INET,    # Internet
                     socket.SOCK_DGRAM)     # UDP
    sock.sendto(xmlFinalMessage, (UDP_IP, UDP_PORT))
    sockv6 = socket.socket(socket.AF_INET6,   # Internet
                     socket.SOCK_DGRAM)     # UDP
    sockv6.sendto(xmlFinalMessage, (UDP_IPV6, UDP_PORT))
except:
    print "Failed sending the message:"
    raise

