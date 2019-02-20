from flask import Flask, jsonify, request, Response
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
        try:
            post_id = post_id.encode('ascii','ignore')
            result = client.query(q.get(q.ref(q.class_("posts"), post_id)))
        except fauna_error.NotFound as e:
            app.logger.debug(e)
            return Response(jsonify('Failed to fetch a post.'), status=404, mimetype='application/json')
        except Exception as e:
            app.logger.debug(e)
            return Response(jsonify('Failed to fetch a post.'), status=500, mimetype='application/json')

        return Response(json.dumps(to_json(result)), status=200, mimetype='application/json')

    """
    Delete a post:
    curl -XDELETE 'http://localhost:8080/post/223590710032466432'
    """
    def delete(self, post_id):
        try:
            post_id = post_id.encode('ascii','ignore')
            result = client.query(q.delete(q.ref(q.class_("posts"), post_id)))
        except fauna_error.NotFound as e:
            app.logger.debug(e)
            return Response(jsonify('Failed to delete a post.'), status=404, mimetype='application/json')
        except Exception as e:
            app.logger.debug(e)
            return Response(jsonify('Failed to delete a post.'), status=500, mimetype='application/json')

        return Response(json.dumps(to_json(result)), status=204, mimetype='application/json')

    """
    Update a post:
    curl -XPOST -H "Content-type: application/json" -d '{
        "title": "My dog and other wonders",
        "id": "223590710032466432"
    }' 'http://localhost:8080/post/'
    """
    def post(self, post_id = None):
        request_json = request.get_json()

        data = {}
        action = "create"

        if "id" in request_json:
            data['id'] = request_json['id']
            action = "update"
        if "title" in request_json:
            data['title'] = request_json['title']
        if "tags" in request_json:
            data['tags'] = request_json['tags']

        query = q.create(q.class_expr("posts"), data)

        try:
            result = client.query(query)
        except fauna_error.NotFound as e:
            app.logger.debug(e)
            return Response(jsonify('Failed to ' + action + ' a post.'), status=404, mimetype='application/json')
        except Exception as e:
            app.logger.debug(e)
            return Response(jsonify('Failed to ' + action + ' a post.'), status=500, mimetype='application/json')

        return Response(json.dumps(to_json(result)), status=201, mimetype='application/json')

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
            app.logger.debug(e)
            return Response(jsonify('Failed to fetch posts.'), status=500, mimetype='application/json')

        return Response(json.dumps(to_json(result)), status=200, mimetype='application/json')


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
                app.logger.debug(json)
                return Response(jsonify('Invalid post information.'), status=422, mimetype='application/json')

        for post in json['posts']:
            if not "title" in post:
                return Response(jsonify('Missing title in post.'), status=422, mimetype='application/json')

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
            return Response(jsonify('Failed to create post(s).'), status=500, mimetype='application/json')

        return Response(json.dumps(to_json(result)), status=200, mimetype='application/json')


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
