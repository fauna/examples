from flask import Flask, jsonify, request
from flask_restful import reqparse, abort, Api, Resource

app = Flask(__name__)
api = Api(app)

from faunadb.client import FaunaClient
from faunadb._json import to_json, parse_json
from faunadb import query as q, errors as fauna_error
from faunadb.client_logger import logger

# Used for handling Fauna secret key presence.
import os, sys
import json

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
Retrieve, delete, or update a single post, per method.
"""
class Post(Resource):
    """
    Retrieve a post:
    curl -XGET 'http://localhost:8080/post/223590710032466432'
    """
    def get(self, post_id):
        print("single post, get")
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
    Delete a post:
    curl -XDELETE 'http://localhost:8080/post/223590710032466432'
    """
    def delete(self, post_id):
        try:
            post_id = post_id.encode('ascii','ignore')
            result = client.query(q.delete(q.ref(q.class_("posts"), post_id)))
        except Exception as e:
            print (e)
            response = jsonify('Failed to delete a post.')
            response.status_code = 500
            return response

        response = jsonify('Post deleted successfully!')
        response.status_code = 204
        return response

    """
    Update a post:
    curl -XPOST -H "Content-type: application/json" -d '{
        "title": "My dog and other wonders",
        "id": "223590710032466432"
    }' 'http://localhost:8080/post/'
    """
    def post(self, post_id = None):
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
Gets a set of posts by the title via the posts_by_title index, or returns all posts if no parameter is 
passed.
Create a set of posts with put.
"""
class PostList(Resource):
    """
    Retrieve a list of posts.
    curl -XGET 'http://localhost:8080/posts/'
    curl -XGET 'http://localhost:8080/posts/My%20dog%20and%20other%20wonders'
    """
    def get(self, post_title = None):
        try:
            if post_title:
                posts = client.query(q.map_(lambda x: q.get(x), q.paginate(q.match(q.index("posts_by_title"), post_title))))
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
    Create a set of posts - title is required, tags are optional.
    Single post data can be optionally wrapped in a list.
    Examples:
    curl -XPUT -H "Content-type: application/json" -d '{
      "title": "My cat and other marvels"
     }' 'http://localhost:8080/posts/'

    curl -XPUT -H "Content-type: application/json" -d '[{
      "title": "My cat and other marvels",
      "tags": ["pet", "cute"]
     }]' 'http://localhost:8080/posts/'

     curl -XPUT -H "Content-type: application/json" -d '{"posts": [
        {"title": "My cat and other marvels", "tags": ["pet", "cute"]},
        {"title": "Pondering during a commute", "tags": ["commuting"]},
        {"title": "Deep meanings in a latte", "tags": ["coffee"]}
     ]}' 'http://localhost:8080/posts/'
    """
    def put(self, post_title = None):
        json = request.get_json()
        data = []

        # validate and format incoming data
        if not "posts" in json:

            # if "posts" has been omitted and this is just a list of post data, reformat
            if isinstance(json, list) and "title" in json[0]:
                json = {"posts": json}
            elif not isinstance(json, list) and "title" in json:
                json_list = []
                json_list.append(json)
                json = {"posts": json_list}
            else:
                print(json)
                response = jsonify('Invalid post information.')
                response.status_code = 422
                return response

        for post in json['posts']:
            if not "title" in post:
                response = jsonify('Missing title in post.')
                response.status_code = 422
                return response

            data.append({"title": post['title'], "tags": post.get('tags', [])})

        query = q.map_expr(
            lambda post_data: q.create(
                q.class_expr("posts"),
                {"data": {"title": post_data}}
            ),
            data
        )

        try:
            result = client.query(query)
        except Exception as e:
            app.logger.debug(e)
            response = jsonify('Failed to create post(s).')
            response.status_code = 500
            return response

        # to_json makes it a string, rather than a json object, which flask expects
        response = jsonify(to_json(result))
        response.status_code=201
        return response

##
## Actually setup the Api resource routing here
##
api.add_resource(PostList, '/posts', '/posts/', '/posts/<string:post_title>')
api.add_resource(Post, '/post', '/post/', '/post/<string:post_id>')

# Default handler, returns a 404 error for invalid routes.
@app.errorhandler(404)
def not_found(error=None):
    print("Route not found:")
    print(request)
    message = {
        'status': 404,
        'message': 'Not Found: ' + request.url,
    }
    resp = jsonify(message)
    resp.status_code = 404

    return resp

if __name__ == "__main__":
    app.run(port=8080)
