#!/usr/bin/env python

"""Top-level script to invoke helloworld implementation."""

import sys
import helloworld.main
import random
import metrics.export_metric as metrics

if __name__ == '__main__':
    elem_count = random.randrange(100, 1000)
    metrics.export(elem_count)
    sys.exit(helloworld.main.main())
