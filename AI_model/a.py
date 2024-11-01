import json
import numpy as np
import cv2
from tensorflow.keras.models import Sequential # type: ignore
from tensorflow.keras.layers import Conv3D, MaxPooling3D, TimeDistributed, Flatten, LSTM, Dense

# JSON 파일 경로
#json_file_path = r"C:\Users\82105\Desktop\school\final_project\accident_av_data\New_Sample\labeling_data\accident_rate_json\bb_1_120926_two-wheeled-vehicle_223_24820.json"
json_file_path = input("json 파일경로 입력하세요 : ")

# JSON 파일 읽기
with open(json_file_path, 'r') as f:
    data = json.load(f)

# 과실 비율 추출
negligence_rateA = data['video']['accident_negligence_rateA']
negligence_rateB = data['video']['accident_negligence_rateB']
target = np.array([negligence_rateA, negligence_rateB])  # 타겟 변수

#video_path = r"C:\Users\82105\Desktop\school\final_project\accident_av_data\New_Sample\original_data\test_case_av\bb_1_120926_two-wheeled-vehicle_223_24820.mp4"
video_path = input("비디오경로 입력하세요 : ")

cap = cv2.VideoCapture(video_path)  # 주어진 video_path로 비디오 캡처 객체를 생성합니다.
    
frames = []  # 프레임을 저장할 빈 리스트를 초기화합니다.
frame_rate = int(cap.get(cv2.CAP_PROP_FPS))  # 비디오의 초당 프레임 수(FPS)를 가져옵니다.
count = 0  # 프레임 카운터를 초기화합니다.

#interval : 몇초마다 하나의 프레임을 추출할것인지
interval = 5
while cap.isOpened():  # 비디오 캡처 객체가 열려 있는 동안 루프를 계속합니다.
    ret, frame = cap.read()  # 비디오에서 프레임을 읽습니다. ret은 읽기 성공 여부를 나타냅니다.
    if not ret:  # 프레임을 읽지 못했을 경우 루프를 종료합니다.
        print("cap read err")
        break

    # 매 interval초마다 1개의 프레임을 추출합니다.
    if count % (frame_rate * interval) == 0:
        frame = cv2.resize(frame, (244, 244))  # CNN 입력 크기로 조정
        frames.append(frame)  # 조건이 참일 경우 프레임을 리스트에 추가합니다.
    count += 1  # 프레임 카운터를 증가시킵니다.

cap.release()  # 비디오 캡처 객체를 해제합니다.

# Numpy 배열로 변환
frames_array = np.array(frames)  # (프레임 수, 높이, 너비, 채널)
sequence_length = 10  # 시퀀스 길이
X = []
y = []

for i in range(len(frames_array) - sequence_length):
    X.append(frames_array[i:i + sequence_length])  # 시퀀스 생성
    y.append(target)  # 타겟 추가

X = np.array(X)  # (샘플 수, 시퀀스 길이, 높이, 너비, 채널)
y = np.array(y)  # (샘플 수, 타겟 크기)

# Height, width, channels 정의
height = 64
width = 64
channels = 3  # RGB 이미지의 경우

#모델 학습
model = Sequential()
model.add(TimeDistributed(Conv3D(filters=32, kernel_size=(3, 3, 3), activation='relu'), input_shape=(sequence_length, height, width, channels)))
model.add(TimeDistributed(MaxPooling3D(pool_size=(2, 2, 2))))
model.add(TimeDistributed(Flatten()))
model.add(LSTM(50))
model.add(Dense(2, activation='softmax'))  # 타겟 크기에 따라 조정

model.compile(optimizer='adam', loss='categorical_crossentropy', metrics=['accuracy'])
model.fit(X, y, epochs=10, batch_size=32)

#test
print(f"A 과실비율 : {negligence_rateA}")
print(f"B 과실비율 : {negligence_rateB}")
print(f"추출된 프레임 수: {len(frames)}")  # 추출된 프레임 수 출력

# 모든 프레임을 순차적으로 표시하기
for i, frame in enumerate(frames):
    cv2.imshow(f"Extracted Frame {i+1}", frame)  # 각 프레임을 표시
    if cv2.waitKey(3000) & 0xFF == ord('q'):  # 30ms 대기 후 다음 프레임 표시, 'q'로 중지
        break
    
cv2.destroyAllWindows()  # 모든 윈도우를 닫기