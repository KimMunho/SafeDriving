import os
import cv2
import numpy as np
import json
from sklearn.model_selection import train_test_split
from tensorflow.keras import layers, models, Input
from tensorflow.keras.utils import to_categorical
from tensorflow.keras.optimizers import Adam
from tensorflow.keras.models import load_model
from tensorflow.keras.callbacks import EarlyStopping
from sklearn.metrics import mean_squared_error, mean_absolute_error, accuracy_score

# 프레임을 지정된 크기로 조정하는 함수
def resize_frames(frames, size=(224, 224)):
    return [cv2.resize(frame, size) for frame in frames]

# 프레임을 정규화하는 함수 (픽셀 값을 [0, 1] 범위로 조정)
def normalize_frames(frames):
    return [frame / 255.0 for frame in frames]

# 비디오에서 프레임을 추출하는 함수
def extract_frames(video_path, interval=0.1): 
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
            frames.append(frame)
        count += 1

    cap.release()
    return frames

# JSON 파일에서 과실 비율 추출 후 타겟변수 설정
def json_to_accident_rate(json_path):
    with open(json_path, 'r') as f:
        data = json.load(f)
    
    if 'accident_negligence_rateA' in data['video'] and 'accident_negligence_rateB' in data['video']:
        negligence_rate = data['video']['accident_negligence_rateA']
    else:
        negligence_rate = data['video']['accident_negligence_rate']
    
    return negligence_rate

# CNN-LSTM 모델 정의 (Dropout 조정)
def create_cnn_lstm_model(frame_shape=(224, 224, 3), sequence_length=10, num_classes=81):
    cnn_base = models.Sequential([
        layers.Conv2D(32, (3, 3), activation='relu', input_shape=frame_shape),
        layers.MaxPooling2D((2, 2)),
        layers.Conv2D(64, (3, 3), activation='relu'),
        layers.MaxPooling2D((2, 2)),
        layers.Conv2D(128, (3, 3), activation='relu'),
        layers.Flatten(),
        layers.Dropout(0.3)  # Dropout 값 조정
    ])
    
    input_sequence = Input(shape=(sequence_length, *frame_shape))
    cnn_features = layers.TimeDistributed(cnn_base)(input_sequence)
    lstm_out = layers.LSTM(64, activation='relu')(cnn_features)
    lstm_out = layers.Dropout(0.3)(lstm_out)  # LSTM 뒤에 Dropout 추가
    output = layers.Dense(num_classes, activation='softmax')(lstm_out)
    
    model = models.Model(inputs=input_sequence, outputs=output)
    model.compile(optimizer=Adam(learning_rate=0.0001), loss='categorical_crossentropy', metrics=['accuracy'])  # 학습률 조정
    return model

# 학습 데이터 준비
def prepare_data(frames, label, sequence_length=10, num_classes=81):
    frames_resized = resize_frames(frames)
    frames_normalized = normalize_frames(frames_resized)

    X, y = [], []
    for i in range(0, len(frames_normalized) - sequence_length + 1):
        X.append(frames_normalized[i:i + sequence_length])
        y.append(label)  # 모든 시퀀스에 동일한 레이블 사용
    
    X = np.array(X)
    y = np.array(y)
    y = to_categorical(y, num_classes=num_classes)  # 단일 클래스 레이블로 인코딩

    return X, y

# 폴더 내 모든 mp4와 json 파일 학습
def train_on_folder(folder_path):
    model_path = 'cnn_lstm_model.h5'
    if os.path.exists(model_path):
        print("저장된 모델을 불러옵니다.")
        model = load_model(model_path)
    else:
        print("새 모델을 생성합니다.")
        model = create_cnn_lstm_model(frame_shape=(224, 224, 3), sequence_length=10, num_classes=81)

    # Early Stopping 콜백 정의
    early_stopping = EarlyStopping(monitor='val_loss', patience=5, restore_best_weights=True)

    files = os.listdir(folder_path)
    video_files = [f for f in files if f.endswith('.mp4')]
    
    for video_file in video_files:
        video_path = os.path.join(folder_path, video_file)
        json_file = video_file.replace('.mp4', '.json')
        json_path = os.path.join(folder_path, json_file)
        
        if not os.path.exists(json_path):
            print(f"해당 JSON 파일을 찾을 수 없습니다: {json_path}")
            continue

        frames = extract_frames(video_path, interval=0.1)
        print(f"{video_file}에서 추출된 프레임 수: {len(frames)}")

        accident_rate = json_to_accident_rate(json_path)
        print(f"{json_file}에서 추출된 과실 비율: {accident_rate}")

        sequence_length = 10
        X, y = prepare_data(frames, accident_rate, sequence_length=sequence_length, num_classes=81)
        
        if len(X) > 1:
            X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)
        else:
            print("데이터 샘플이 부족하여 전체 데이터를 학습에 사용합니다.")
            X_train, y_train = X, y
            X_test, y_test = X, y

        # 모델 학습 시 Early Stopping 적용
        model.fit(X_train, y_train, epochs=100, batch_size=4, validation_split=0.2, callbacks=[early_stopping])
        
        # 모델 저장
        model.save(model_path)
        print(f"{video_file}와 {json_file}에 대해 모델이 저장되었습니다.")

    print("폴더 내 모든 파일에 대한 학습이 완료되었습니다.")

if __name__ == "__main__":
    folder_path = input("폴더 경로를 입력하세요: ")
    train_on_folder(folder_path)
