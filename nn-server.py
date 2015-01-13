#!/usr/bin/python

####################
# Network Notifier #
####################

#Copyright (C) 2015 Jesus Dieguez Fernandez

#This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.

#This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details. You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA or see http://www.gnu.org/licenses/.

import socket
import lxml.etree
import hashlib
from subprocess import call

COMMAND = "notify-send"
UDP_IP = "0.0.0.0"
UDP_PORT = 7415
ENCODING = 'utf-8'

try:
    sock = socket.socket(socket.AF_INET, # Internet
                         socket.SOCK_DGRAM) # UDP
    sock.bind((UDP_IP, UDP_PORT))
except:
    print "Failed to initialice network socket:"
    raise

while True:
    
    data, addr = sock.recvfrom(1024) # buffer size is 1024 bytes

    #print "received message:", data
    try:
        xmlDocument = lxml.etree.XML(data)
        notifications = xmlDocument.findall(".//Notification")
        textFinalMessage = ""
        for notification in notifications:
            textSource = notification.find("Source").text.encode(ENCODING)
            if notification.find("Message").text is None:
                textMessage = ''
            else:
                textMessage = notification.find("Message").text.encode(ENCODING)
            textHash = notification.find("Hash").text.encode(ENCODING)
            if textHash == hashlib.sha256("%s-%s" %(textSource, textMessage)).hexdigest():
                textFinalMessage = "%s: %s" %(textSource, textMessage)
            else:
                print "Corrupted notification, aparently from %s, skipped." % (textSource)
    except:
        print "Failed processing the notification:"
        raise
    
    try:
        if textFinalMessage != "" and textMessage != "":
            call([COMMAND, textFinalMessage])
    except:
        print "Failed to calling the notification utility. Message was:"
        print textFinalMessage
        pass
    
    
    
