package com.pickup.sports.ui.calender.view.data

public fun <T : Comparable<T>> checkRange(start: T, end: T) {
    check(end >= start) {
        "start: $start is greater than end: $end"
    }
}
