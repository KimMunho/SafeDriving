import cv2
import numpy as np
import json

#json 파일에서 과실비율 추출 후 타겟변수 설정
def json_to_accident_rate(json_path):
    with open(json_path, 'r') as f:
        data = json.load(f)
        
    negligence_rateA = data['video']['accident_negligence_rateA'] #json 파일에서 과실비율 추출합니다.
    negligence_rateB = data['video']['accident_negligence_rateB']
    target = np.array([negligence_rateA], [negligence_rateB])
    
    return target


def extract_frames(video_path, interval=1):
    cap = cv2.VideoCapture(video_path)  # 주어진 video_path로 비디오 캡처 객체를 생성합니다.
    
    frames = []  # 프레임을 저장할 빈 리스트를 초기화합니다.
    frame_rate = int(cap.get(cv2.CAP_PROP_FPS))  # 비디오의 초당 프레임 수(FPS)를 가져옵니다.
    count = 0  # 프레임 카운터를 초기화합니다.

    while cap.isOpened():  # 비디오 캡처 객체가 열려 있는 동안 루프를 계속합니다.
        ret, frame = cap.read()  # 비디오에서 프레임을 읽습니다. ret은 읽기 성공 여부를 나타냅니다.
        if not ret:  # 프레임을 읽지 못했을 경우 루프를 종료합니다.
            print("cap read err")
            break

        # 매 interval초마다 1개의 프레임을 추출합니다.
        if count % (frame_rate * interval) == 0:
            frames.append(frame)  # 조건이 참일 경우 프레임을 리스트에 추가합니다.
        count += 1  # 프레임 카운터를 증가시킵니다.

    cap.release()  # 비디오 캡처 객체를 해제합니다.
    
    return frames


#프레임을 CNN모델에 입력시키기 위한 크기조정
def resize_frames(frames, size=(224, 224)):
    return [cv2.resize(frame, size) for frame in frames]


#정규화 : 픽셀값을 [0,1] 범위로 조정 (CNN 모델에서 입력값으로 넣으려면)
def normalize_frames(frames):
    return [frame / 255.0 for frame in frames]


#시퀀스 정렬 및 패딩
def pad_frames(frames, max_length=30):
    if len(frames) < max_length:
        # 부족한 프레임을 0으로 패딩
        frames += [np.zeros_like(frames[0])] * (max_length - len(frames))
    else:
        # 초과 프레임을 잘라내기
        frames = frames[:max_length]
    return frames


def main():
    video_path = input("비디오 파일 경로를 입력하세요: ")  # 사용자로부터 비디오 파일 경로 입력받기
    interval = int(input("프레임 추출 간격(초)을 입력하세요: "))  # 사용자로부터 프레임 추출 간격 입력받기
    
    frames = extract_frames(video_path, interval)  # 주어진 비디오에서 프레임 추출
    
    print(f"추출된 프레임 수: {len(frames)}")  # 추출된 프레임 수 출력

    # 모든 프레임을 순차적으로 표시하기
    for i, frame in enumerate(frames):
        cv2.imshow(f"Extracted Frame {i+1}", frame)  # 각 프레임을 표시
        if cv2.waitKey(3000) & 0xFF == ord('q'):  # 30ms 대기 후 다음 프레임 표시, 'q'로 중지
            break
    
    cv2.destroyAllWindows()  # 모든 윈도우를 닫기


if __name__ == "__main__":
    main()  # 메인 함수 실행