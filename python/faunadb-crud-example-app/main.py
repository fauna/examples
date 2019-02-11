from app import app
from flask import jsonify
from flask import flash, request

from faunadb.client import FaunaClient
from faunadb._json import to_json, parse_json
from faunadb import query as q, errors as fauna_error
from faunadb.client_logger import logger

# Used for handling Fauna secret key presence.
import os, sys

if not 'FAUNA_KEY' in os.environ:
    print("The FAUNA_KEY environment variable must be set to your Fauna Cloud access key.")
    sys.exit()

# Set up the fauna query logger for the Fauna client; shows full details of the query if 
# app.debug is set to True in app.py, currently silent.
def log(logged):
    if app.debug == True:
        print logged

# Create the Fauna client using the default built-in cloud connection.
client = FaunaClient(observer=logger(log), secret=os.environ['FAUNA_KEY'])

"""
Sets or updates a post.  Examples:
Create a post:
curl -XPOST -H "Content-type: application/json" -d '{
  "title": "My cat and other marvels",
  "tags": ["pet", "cute"]
 }' 'http://localhost:8080/posts/'

Update a post:
curl -XPOST -H "Content-type: application/json" -d '{
    "title": "My dog and other wonders",
    "id": "223590710032466432"
}' 'http://localhost:8080/posts/'
"""
@app.route('/posts/', methods=['POST'])
def write_post():
    json = request.get_json()

    data = {}
    action = "create"
    status_code = 201

    if "id" in json:
        data['id'] = json['id']
        action = "update"
        status_code = 201
    if "title" in json:
        data['title'] = json['title']
    if "tags" in json:
        data['tags'] = json['tags']

    query = q.create(q.class_expr("posts"), data)

    try:
        result = client.query(query)
    except Exception as e:
        app.logger.debug(e)
        response = jsonify('Failed to ' + action + ' a post.')
        response.status_code = 500
        return response

    # to_json makes it a string, rather than a json object, which flask expects
    response = jsonify(to_json(result))
    response.status_code=status_code
    return response

"""
Fetches a post by numeric id, a set of posts by the title via the posts_by_title index, or returns
all posts if no parameter is passed.

Examples:
curl -XGET 'http://localhost:8080/posts/'
curl -XGET 'http://localhost:8080/posts/My%20dog%20and%20other%20wonders'
"""
@app.route('/posts')
@app.route('/posts/<string:title>')
def posts(title = None):
    try:
        if title:
            posts = client.query(q.map_(lambda x: q.get(x), q.paginate(q.match(q.index("posts_by_title"), title))))
        else:
            posts = client.query(q.map_(lambda x: q.get(x), q.paginate(q.match(q.index("all_posts")))))

    except Exception as e:
        print (e)
        resp = jsonify('Failed to fetch posts.')
        resp.status_code = 500
        return resp

    response = jsonify(to_json(posts))
    response.status_code = 200
    return response

"""
Fetches a single post, by id.

Example:
curl -XGET 'http://localhost:8080/posts/1549577590110000'
"""
@app.route('/post/<string:post_id>')
def get_post(post_id):
    try:
        post_id = post_id.encode('ascii','ignore')
        result = client.query(q.get(q.ref(q.class_("posts"), post_id)))
    except fauna_error.NotFound as e:
        print (e)
        response = jsonify('Failed to fetch a post.')
        response.status_code = 404
        return response
    except Exception as e:
        print (e)
        response = jsonify('Failed to fetch a post.')
        response.status_code = 500
        return response

    response = jsonify(to_json(result))
    response.status_code = 200
    return response

"""
Deletes a single post, by id.

Example:
curl -XGET 'http://localhost:8080/delete/1549577590110000'
"""
@app.route('/delete/<string:post_id>')
def delete_post(post_id):
    try:
        post_id = post_id.encode('ascii','ignore')
        result = client.query(q.delete(q.ref(q.class_("posts"), post_id)))
    except Exception as e:
        print (e)
        response = jsonify('Failed to delete a post.')
        response.status_code = 500
        return response

    response = jsonify('Post deleted successfully!')
    response.status_code = 200
    return response

# Default handler, returns a 404 error for invalid routes.
@app.errorhandler(404)
def not_found(error=None):
    message = {
        'status': 404,
        'message': 'Not Found: ' + request.url,
    }
    resp = jsonify(message)
    resp.status_code = 404

    return resp
        
if __name__ == "__main__":
    app.run(port=8080)