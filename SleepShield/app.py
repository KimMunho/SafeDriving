import tkinter as tk
import customtkinter as ctk
import torch
import numpy
import cv2
from PIL import Image, ImageTk
import numpy as np
import vlc
import ssl
ssl._create_default_https_context = ssl._create_unverified_context


app = tk.Tk()
app.geometry("600x600")
app.title("Drowsy Boi 4.0")
ctk.set_appearance_mode("dark")

vidFrame = tk.Frame(height=480, width=600)
vidFrame.pack()
vid = ctk.CTkLabel(vidFrame)
vid.pack()

model = torch.hub.load('ultralytics/yolov5', 'yolov5s')  
cap = cv2.VideoCapture(1)
def detect():

    ret, frame =  cap.read()
    frame = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
    results = model(frame)
    img = np.squeeze(results.render())

    imgarr = Image.fromarray(img)
    imgtk = ImageTk.PhotoImage(imgarr)
    vid.imgtk = imgtk
    vid.configure(image=imgtk)
    vid.after(10, detect)

detect()
app.mainloop()