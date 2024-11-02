import cv2
import numpy as np
import json
from sklearn.model_selection import train_test_split
from tensorflow.keras import layers, models
from tensorflow.keras.utils import to_categorical
from tensorflow.keras.optimizers import Adam
from sklearn.metrics import mean_squared_error, mean_absolute_error, accuracy_score

# 프레임을 지정된 크기로 조정하는 함수
def resize_frames(frames, size=(224, 224)):
    return [cv2.resize(frame, size) for frame in frames]

# 프레임을 정규화하는 함수 (픽셀 값을 [0, 1] 범위로 조정)
def normalize_frames(frames):
    return [frame / 255.0 for frame in frames]

def extract_frames(video_path, interval=1):
    cap = cv2.VideoCapture(video_path)
    if not cap.isOpened():
        print("비디오 파일을 열 수 없습니다. 경로와 파일 형식을 확인하세요.")
        return []
    
    frames = []
    frame_rate = int(cap.get(cv2.CAP_PROP_FPS))
    count = 0

    while cap.isOpened():
        ret, frame = cap.read()
        if not ret:
            print("비디오 읽기 완료 또는 오류 발생")
            break
        if count % (frame_rate * interval) == 0:
            frames.append(frame)
        count += 1

    cap.release()
    return frames

# json 파일에서 과실비율 추출 후 타겟변수 설정
def json_to_accident_rate(json_path):
    with open(json_path, 'r') as f:
        data = json.load(f)
    
    # JSON 파일에서 과실 비율을 확인하여 추출
    if 'accident_negligence_rate' in data['video']:
        negligence_rate = data['video']['accident_negligence_rate']
        target = np.array([negligence_rate])
    elif 'accident_negligence_rateA' in data['video']:
        negligence_rateA = data['video']['accident_negligence_rateA']
        target = np.array([negligence_rateA])
    else:
        raise ValueError("JSON 파일에서 올바른 과실 비율 데이터를 찾을 수 없습니다.")
    
    return target

# CNN 모델 정의
def create_cnn_model(input_shape=(224, 224, 3), num_classes=81):  # num_classes 조정
    model = models.Sequential([
        layers.Conv2D(32, (3, 3), activation='relu', input_shape=input_shape),
        layers.MaxPooling2D((2, 2)),
        layers.Conv2D(64, (3, 3), activation='relu'),
        layers.MaxPooling2D((2, 2)),
        layers.Conv2D(128, (3, 3), activation='relu'),
        layers.Flatten(),
        layers.Dense(64, activation='relu'),
        layers.Dense(num_classes, activation='softmax')  # 클래스 수에 맞게 출력 조정
    ])
    model.compile(optimizer=Adam(), loss='categorical_crossentropy', metrics=['accuracy'])
    return model

# 학습 데이터 준비
def prepare_data(frames, labels, num_classes=81):  # num_classes 조정
    frames_resized = resize_frames(frames)
    frames_normalized = normalize_frames(frames_resized)
    X = np.array(frames_normalized)
    y = to_categorical(labels, num_classes=num_classes)
    return X, y

# 모델 예측값과 실제값 비교 및 정확도 측정
def evaluate_model_predictions(y_true, y_pred):
    mse = mean_squared_error(y_true, y_pred)
    mae = mean_absolute_error(y_true, y_pred)
    print(f"Mean Squared Error (MSE): {mse:.2f}")
    print(f"Mean Absolute Error (MAE): {mae:.2f}")

def main():
    video_path = input("비디오 파일 경로를 입력하세요: ")
    json_path = input("JSON 파일 경로를 입력하세요: ")
    interval = int(input("프레임 추출 간격(초)을 입력하세요: "))
    
    frames = extract_frames(video_path, interval)
    print(f"추출된 프레임 수: {len(frames)}")

    # JSON 파일에서 실제 라벨 가져오기
    true_labels = json_to_accident_rate(json_path)
    print("추출된 과실 비율:", true_labels)

    # 데이터 준비 (라벨이 단일 값일 경우 데이터 셋을 맞춰줘야 함)
    labels = np.full(len(frames), true_labels[0])  # 프레임 수만큼 라벨을 동일하게 설정
    
    # 데이터 준비
    X, y = prepare_data(frames, labels, num_classes=81)  # num_classes 조정

    # 학습 및 테스트 데이터로 분할
    X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)

    # 모델 생성 및 훈련
    model = create_cnn_model(input_shape=(224, 224, 3), num_classes=81)  # num_classes 조정
    model.fit(X_train, y_train, epochs=5, batch_size=16, validation_split=0.1)

    # 예측 및 정확도 평가
    y_pred = model.predict(X_test)
    y_pred_classes = np.argmax(y_pred, axis=1)
    y_test_classes = np.argmax(y_test, axis=1)

    accuracy = accuracy_score(y_test_classes, y_pred_classes)
    print(f"테스트 데이터 정확도: {accuracy:.2f}")

    # 모델 평가
    evaluate_model_predictions(y_test_classes, y_pred_classes)

if __name__ == "__main__":
    main()
