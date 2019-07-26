#!/usr/bin/python
import sys, getopt, Adafruit_DHT
from faunadb import query as q
from faunadb.objects import Ref
from faunadb.client import FaunaClient
from datetime import datetime

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

result = client.query(q.map_(lambda x: q.get(x), q.paginate(q.match(q.index("all_readings")))))

for obj in result['data']:
  ts = obj['ts'] / 1000000.0
  time = datetime.utcfromtimestamp(ts).strftime('%Y-%m-%d %H:%M:%S')
  print 'Time: {0}  Temp: {1:0.1f}C  Humidity: {2:0.1f}%'.format(time, obj['data']['temperature'], obj['data']['humidity'])

