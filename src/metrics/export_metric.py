import time
from prometheus_client.core import GaugeMetricFamily, REGISTRY
from prometheus_client import start_http_server


class MetricsCollector(object):
    def __init__(self, exp_count):
        self.exp_count = exp_count
        
    # Получаем метрики и публикуем их через локальный веб-сервер для сбора сервером Prometheus'a
    def collect(self):
        gauge = GaugeMetricFamily("custom_metric", "Custom metrics example", labels=["custom_metric"])
        gauge.add_metric(['custom_metric'], self.exp_count)
        yield gauge


def export(exported_count):
    # Порт, на котором будет подниматься веб-сервер
    port = 9000
    start_http_server(port)
    REGISTRY.register(MetricsCollector(exported_count))

    # Даём время серверу Прометея для сбора метрик
    time.sleep(20)
