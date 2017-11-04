# Stats Fetchers
# TODO: Redis integration

from __future__ import print_function

import time
from abc import abstractmethod
from threading import Thread

import psutil


EXITING = False


class LinkedListNode(object):
    def __init__(self, val):
        self.val = val
        self.prev = None
        self.next = None

    def set_prev(self, prev_node):
        self.prev = prev_node

    def set_next(self, next_node):
        self.next = next_node

    def get_value(self):
        return self.val


class LinkedList(object):
    def __init__(self):
        self.head = None
        self.tail = None
        self.size = 0

    def __repr__(self):
        repr_str = ""
        cur_node = self.head
        i = 0
        while i < self.size:
            repr_str += str(cur_node.val)
            if i+1 != self.size:
                repr_str += ", "
            cur_node = cur_node.next
            i += 1
        repr_str = "[" + repr_str + "]"
        return repr_str

    def get_size(self):
        return self.size

    def insert_head(self, x):
        node = LinkedListNode(x)
        if self.head:
            self.head.prev = node
            node.next = self.head
        else:
            self.tail = node
        self.head = node
        self.size += 1

    def delete_head(self):
        if self.tail == self.head:
            self.tail = None
        if self.head:
            next_head = self.head.next
            if next_head:
                next_head.prev = None
            self.head = next_head
        if self.size > 0:
            self.size -= 1

    def get_head(self):
        return self.head

    def insert_tail(self, x):
        node = LinkedListNode(x)
        if self.tail:
            self.tail.next = node
            node.prev = self.tail
        else:
            self.head = node
        self.tail = node
        self.size += 1

    def delete_tail(self):
        if self.head == self.tail:
            self.head = None
        if self.tail:
            next_tail = self.tail.prev
            if next_tail:
                next_tail.next = None
            self.tail = next_tail
        if self.size > 0:
            self.size -= 1

    def get_tail(self):
        return self.tail

    def clear(self):
        self.head = None
        self.tail = None
        self.size = 0

class AttributeValue(object):
    def __init__(self, timestamp, name, value):
        self.timestamp = timestamp
        self.name = name
        self.value = value

    def get_timestamp(self):
        return self.timestamp

    def get_name(self):
        return self.name

    def get_value(self):
        return self.value

    def get_value_str(self):
        return str(self.value)


class AttributeFetcher(object):
    def __init__(self, collect_interval):
        self.collect_interval = collect_interval

    @abstractmethod
    def fetch_attribute_value(self):
        pass

    def get_worker(self):
        fetcher = self
        class worker(Thread):
            def run(self):
                while True:
                    if EXITING:
                        break
                    time.sleep(fetcher.collect_interval)
                    attr = fetcher.fetch_attribute_value()
                    print(
                        "{0:f} - {1:s}: {2:s}".format(
                            attr.get_timestamp(),
                            attr.get_name(),
                            attr.get_value_str()
                        )
                    )
        return worker()

class UsersFetcher(AttributeFetcher):
    def fetch_attribute_value(self):
        all_users = set([])
        ttys = psutil.users()
        for session in ttys:
            all_users.add(session.name)
        return AttributeValue(time.time(), 'users', all_users)


class NumericAttributeFetcher(AttributeFetcher):
    def __init__(self, collect_interval, average_period):
        AttributeFetcher.__init__(self, collect_interval)
        if average_period < 0 or collect_interval < 0 or collect_interval > average_period:
            raise Exception('AttributeFetcher: Wrong ctor arguments')
        self.average_period = average_period
        self.average_window = LinkedList()

    def average_window_period(self):
        if self.average_window.get_size() < 2:
            return 0
        head_ts = self.average_window.get_head().get_value().get_timestamp()
        tail_ts = self.average_window.get_tail().get_value().get_timestamp()
        return head_ts - tail_ts

    def clear_average_window(self):
        while self.average_window_period() > self.average_period:
            self.average_window.delete_tail()

    def get_average(self):
        list_size = self.average_window.get_size()
        if list_size == 0:
            return None
        total = 0.0
        i = 0
        cur_node = self.average_window.get_head()
        while i < list_size:
            total += cur_node.get_value().get_value()
            i += 1
            cur_node = cur_node.next
        return AttributeValue(time.time(), 'avgtest', total / list_size)

    def get_worker(self):
        fetcher = self
        class worker(Thread):
            def run(self):
                while True:
                    if EXITING:
                        break
                    time.sleep(fetcher.collect_interval)
                    attr = fetcher.fetch_attribute_value()
                    print(
                        "{0:f} - {1:s}: {2:s}".format(
                            attr.get_timestamp(),
                            attr.get_name(),
                            attr.get_value_str()
                        )
                    )
                    fetcher.clear_average_window()
                    if fetcher.average_window.get_head():
                        avg_window_head = fetcher.average_window.get_head().get_value()
                        if avg_window_head.get_timestamp() < attr.get_timestamp():
                            fetcher.average_window.insert_head(attr)
                    else:
                        fetcher.average_window.insert_head(attr)
                    if fetcher.average_window_period() >= fetcher.average_period:
                        average_attr = fetcher.get_average()
                        if average_attr:
                            print(
                                "{0:f} - AVERAGE {1:s}: {2:s}".format(
                                    average_attr.get_timestamp(),
                                    average_attr.get_name(),
                                    average_attr.get_value_str()
                                )
                            )
                            fetcher.average_window.clear()
        return worker()


class CPUFetcher(NumericAttributeFetcher):
    def fetch_attribute_value(self):
        return AttributeValue(time.time(), 'cpu_percent', psutil.cpu_percent())


if __name__ == '__main__':
    uf = UsersFetcher(5)
    uf.get_worker().start()
    cpuf = CPUFetcher(1, 10)
    cpuf.get_worker().start()
    while True:
        try:
            time.sleep(1)
        except KeyboardInterrupt:
            print('Quitting...')
            EXITING = True
            break
