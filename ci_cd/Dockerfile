FROM python:3.10.12-slim-bullseye
USER root
RUN mkdir /python
WORKDIR /python
COPY ../source .
RUN python3 helloworld.py
