FROM python:3.10.12-slim-bullseye
USER root
RUN mkdir /python
RUN pwd
WORKDIR /python
COPY src/ .
RUN ls -la
RUN pip3 install -r requirements.txt
RUN apt autoremove
ENTRYPOINT [ "python3", "helloworld.py" ]
