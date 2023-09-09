FROM python:3.10.12-slim-bullseye
USER root
RUN mkdir /python
WORKDIR /python
COPY ./src/ .
RUN pip3 install -r requirements.txt
RUN python3 helloworld.py
