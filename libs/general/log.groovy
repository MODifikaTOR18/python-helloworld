// Green colored info message
def info(message) {
    echo "\033[1;32mINFO: ${message}\033[0m"
}

// Yellow colored warning message
def warning(message) {
    echo "\033[1;33mWARNING: ${message}\033[0m"

}

// Red colored error message
def error(message) {
    echo "\033[1;31mERROR: ${message}\033[0m"
}