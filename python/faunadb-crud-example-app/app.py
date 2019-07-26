#!/usr/bin/python

from flask import Flask

app = Flask(__name__)
# Enable to see debug messages.
# app.debug = True
app.url_map.strict_slashes = False