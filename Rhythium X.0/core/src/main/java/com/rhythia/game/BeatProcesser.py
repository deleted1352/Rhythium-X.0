import numpy as np
import librosa
import os
import sys
import random


def load(file_name):
    #path = os.path.normpath(file_name) 
    #path = path.replace("Rhythium-X.0", "Rhythium X.0")
    print(f"Attempting to load: {file_name}")
    y, sr = librosa.load(file_name)
    tempo_dynamic = librosa.feature.tempo(y=y, sr=sr, aggregate=None, std_bpm=6) #std_bpm is currently relatively low

    tempo, beats_dynamic = librosa.beat.beat_track(y=y, sr=sr, units='time',
                                                bpm=tempo_dynamic, trim=False)



    #seconds, tile_pos
    return beats_dynamic

def process(beats_dynamic):

    distances = []
    for i in range(len(beats_dynamic)-1):
        distances.append(beats_dynamic[i+1] - beats_dynamic[i])

    mean = np.array(distances).mean()


    #match patterns to beats - round of 3, 6, 8

    coords = []

    i = 0

    smalls = [[1, 3, 5], [4, 4, 1]]
    meds = [[4, 4, 4, 0, 2, 7], [0, 1, 2, 6, 7 ,8], [0, 0, 2, 2, 4 ,4]]
    while(i < len(distances) - 3):

        if np.array(distances[i: i + 9]).mean() < mean * 0.93: #close beats -> spiral
            i += 8
            coords.extend([0, 1, 2, 4, 8, 7, 6, 3])

        elif np.array(distances[i: i + 3]).mean() > mean * 1.15: #slow beats -> triangle
            i += 3
            coords.extend(random.choice(smalls))
        else:
            i+= 6
            coords.extend(random.choice(meds))
    return coords
def export(file_name, beats_dynamic, coords):
    # remove the .mp3 extension before adding .txt
    base_name = os.path.splitext(file_name)[0]
    output_path = base_name + ".txt"

    print(f"Exporting to: {output_path}")
    with open("example.txt", "w"):
        pass #clear


    with open(output_path, 'w') as f:
        
        for i in range(min(len(beats_dynamic), len(coords))):
            f.write(f"{beats_dynamic[i]},{coords[i]}\n")

def main():
    
    command = sys.argv[1]
    if command != "process_beats":
        print(f"unknown command: {command}")
        sys.exit(1)

    file_name = sys.argv[2]
    if not os.path.isfile(file_name):
        print(f"audio file not found: {file_name}")
        sys.exit(1)

    print("got the command")
    beats_dynamic = load(file_name)
    print("loaded")
    coords = process(beats_dynamic)

    export(file_name, beats_dynamic, coords)
    print("exported")
if __name__ == "__main__":
    main()