from flask import Flask, request, jsonify


app = Flask(__name__)


# Structure: { "song_title": [ {"player": "name", "score": 1000}, ... ] }
leaderboards = {}


@app.route('/submit_score', methods=['POST'])
def submit_score():
   data = request.json
   song = data.get('song')
   player = data.get('player')
   score = data.get('score')


   if song not in leaderboards:
       leaderboards[song] = []


   leaderboards[song].append({"player": player, "score": score})
   # sort by score descending
   leaderboards[song] = sorted(leaderboards[song], key=lambda x: x['score'], reverse=True)


   return jsonify({"status": "success", "message": "Score saved!"}), 200


@app.route('/leaderboard/<song_title>', methods=['GET'])
def get_leaderboard(song_title):
   scores = leaderboards.get(song_title, [])
   # return top 10
   return jsonify(scores[:10]), 200


if __name__ == '__main__':
   app.run(host='0.0.0.0', port=5000)

