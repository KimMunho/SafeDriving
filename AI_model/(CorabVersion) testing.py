import os
import cv2
import json
import numpy as np
from tensorflow.keras.models import load_model
from tensorflow.keras.utils import to_categorical
from tensorflow.keras.layers import InputLayer  # InputLayer를 불러오기 위한 import
from sklearn.metrics import mean_squared_error, mean_absolute_error, accuracy_score
from google.colab import drive

# Google Drive 마운트
drive.mount('/content/drive')

# 경로 설정
folder_path = '/content/drive/MyDrive/SafeDriving/TesingData'  # 테스트 데이터 폴더 경로
model_path = '/content/drive/MyDrive/SafeDriving/AI_Model/Model_Number_2.h5'  # Colab에서 저장한 모델 경로

# 테스트 데이터 준비 함수
def prepare_test_data(video_path, json_path, sequence_length=10, num_classes=81):
    frames = extract_frames(video_path, interval=0.1)
    frames_resized = resize_frames(frames)
    frames_normalized = normalize_frames(frames_resized)

    label = json_to_accident_rate(json_path)
    X, y = [], []
    for i in range(0, len(frames_normalized) - sequence_length + 1):
        X.append(frames_normalized[i:i + sequence_length])
        y.append(label)

    X = np.array(X)
    y = np.array(y)
    y = to_categorical(y, num_classes=num_classes)

    return X, y

# JSON 파일에서 과실 비율 추출 후 타겟변수 설정
def json_to_accident_rate(json_path):
    with open(json_path, 'r') as f:
        data = json.load(f)

    if 'accident_negligence_rateA' in data['video'] and 'accident_negligence_rateB' in data['video']:
        negligence_rate = data['video']['accident_negligence_rateA']
    else:
        negligence_rate = data['video']['accident_negligence_rate']

    return negligence_rate

# 프레임을 지정된 크기로 조정하는 함수
def resize_frames(frames, size=(224, 224)):
    return [cv2.resize(frame, size) for frame in frames]

# 프레임을 정규화하는 함수
def normalize_frames(frames):
    return [frame / 255.0 for frame in frames]

# 프레임 추출 함수
def extract_frames(video_path, interval=0.2):
    cap = cv2.VideoCapture(video_path)
    if not cap.isOpened():
        print(f"비디오 파일을 열 수 없습니다: {video_path}")
        return []

    frames = []
    frame_rate = int(cap.get(cv2.CAP_PROP_FPS))
    count = 0

    while cap.isOpened():
        ret, frame = cap.read()
        if not ret:
            print(f"비디오 읽기 완료 또는 오류 발생: {video_path}")
            break
        if count % int(frame_rate * interval) == 0:
            frame = cv2.resize(frame, (112, 112))
            frames.append(frame)
        count += 1

    cap.release()
    return frames

# 폴더 내 모든 데이터로 정확도 측정
def evaluate_model_on_folder(model_path, folder_path, sequence_length=10, num_classes=81):
    # 모델 로드 시 custom_objects에 InputLayer를 추가하여 batch_shape 오류 해결
    model = load_model(model_path, custom_objects={'InputLayer': InputLayer}, compile=False)

    files = os.listdir(folder_path)
    video_files = [f for f in files if f.endswith('.mp4')]

    # 정확도 계산을 위한 변수 초기화
    all_y_test = []
    all_y_pred = []

    for video_file in video_files:
        video_path = os.path.join(folder_path, video_file)
        json_file = video_file.replace('.mp4', '.json')
        json_path = os.path.join(folder_path, json_file)

        # JSON 파일이 없는 경우 스킵
        if not os.path.exists(json_path):
            print(f"해당 JSON 파일을 찾을 수 없습니다: {json_path}")
            continue

        # 테스트 데이터 준비
        X_test, y_test = prepare_test_data(video_path, json_path, sequence_length=sequence_length, num_classes=num_classes)

        # 예측 수행
        y_pred = model.predict(X_test)

        # 예측 및 실제 레이블 저장
        y_test_labels = np.argmax(y_test, axis=1)
        y_pred_labels = np.argmax(y_pred, axis=1)

        # 각 비디오 파일에 대한 예측 값과 실제 값 출력
        print(f"\n파일명: {video_file}")
        print(f"실제 과실 비율 (negligence_rate): {y_test_labels[0] * 10}")
        print(f"모델 예측 과실 비율: {np.round(np.mean(y_pred_labels) * 10)}")  # 평균 예측 값으로 반올림

        # 전체 데이터에 대한 정확도 계산을 위해 수집
        all_y_test.extend(y_test_labels)
        all_y_pred.extend(y_pred_labels)

    # 전체 데이터에 대한 정확도 및 오차 계산
    accuracy = accuracy_score(all_y_test, all_y_pred)
    mse = mean_squared_error(all_y_test, all_y_pred)
    mae = mean_absolute_error(all_y_test, all_y_pred)

    print(f"\n전체 폴더에 대한 정확도: {accuracy:.2f}")
    print(f"전체 폴더에 대한 MSE: {mse:.2f}")
    print(f"전체 폴더에 대한 MAE: {mae:.2f}")

# 평가 실행
evaluate_model_on_folder(model_path, folder_path)
