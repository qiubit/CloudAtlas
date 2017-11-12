from flask import Flask, request, Response
from flask_restful import Resource, Api
from json import dumps
from flask.ext.jsonpify import jsonify
from flask.ext.restful.utils import cors


ZONES = {
    '/': {'level': 0, 'contacts': set(['/a', '/b', '/c']), 'name': 'root', 'status': 'OK', 'rootMeta': 'hej'},
    '/bruna': {'level': 1, 'contacts': set(['/bruna/a', '/bruna/b']), 'name': 'brunaHQ', 'status': 'OK', 'brunaMeta': 'ho'},
    '/bruna/1': {'level': 2, 'contacts': set(['/bruna/a', '/bruna/b']), 'name': 'brunaHQ', 'status': 'OK', '1Meta': 'hhoho'},
}

ZONE_IDS = {
    0: '/',
    1: '/bruna',
    2: '/bruna/1',
}


app = Flask(__name__)
api = Api(app)


class Zones(Resource):

    @cors.crossdomain(origin='*')
    def get(self):
        return jsonify({ 'zones': [0, 1, 2] })


#class Zone(Resource):
    #def get(self, zone_id):



class Attributes(Resource):
    def get(self, zone_id):
        # return jsonify({ 'attributes': ZONES[zone_id].keys() })
        return jsonify({ 'attributes': ZONES[ZONE_IDS[zone_id]].keys() })


class Contacts(Resource):
    @cors.crossdomain(origin='*')
    def get(self, zone_id):
        return jsonify({ 'contacts': list(ZONES[ZONE_IDS[zone_id]]['contacts']) })

    @cors.crossdomain(origin='*')
    def put(self, zone_id):
        global ZONES
        request.data = eval(request.data)
        ZONES[ZONE_IDS[zone_id]]['contacts'] = set(request.data['contacts'])

    def options(self, zone_id):
        resp = Response("Foo bar baz")
        resp.headers['Access-Control-Allow-Origin'] = '*'
        resp.headers['Access-Control-Allow-Methods'] = 'PUT, HEAD, OPTIONS, GET'
        resp.headers['Access-Control-Allow-Headers'] = 'Content-Type'
        return resp


api.add_resource(Zones, '/zones')
api.add_resource(Attributes, '/attributes/<int:zone_id>')
api.add_resource(Contacts, '/contacts/<int:zone_id>')


if __name__ == '__main__':
    app.run(port='5002')