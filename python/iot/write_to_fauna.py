#!/usr/bin/python

# See README.md for detailed instructions.

import sys, Adafruit_DHT, getopt
from faunadb import query as q
from faunadb.objects import Ref
from faunadb.client import FaunaClient

try:
  opts,args = getopt.getopt(sys.argv[1:],"hs:",["secret=", "help"])
except:
  print 'read.py --secret <faunaSecret>'

for opt, arg in opts:
  if opt in ('-h', '--help'):
    print 'Usage: read.py --secret <faunaSecret>'
    sys.exit()
  elif opt in ("-s", "--secret"):
    secretParam = arg
  else:
    print 'Usage: read.py --secret <faunaSecret>'
    sys.exit()

# create the fauna client using the provided secret
client= FaunaClient(secret=secretParam)

while True:
  humidity, temperature = Adafruit_DHT.read_retry(11, 4)

  # query the database based on the client
  # q.class_expr value is the Class on fauna
  # The "data" value is written directly to the database.
  client.query(
    q.create(
      q.class_expr("readings"),
      {"data": {"temperature":temperature, "humidity":humidity}}
    )
  )
  print 'Wrote temperature: {0:0.1f}C, humidity: {1:0.1f}%'.format(temperature, humidity)
