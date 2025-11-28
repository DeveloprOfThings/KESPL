package io.github.developrofthings.kespl.collection

import kotlin.jvm.Transient

interface ByteList : List<Byte> {
    fun addBytes(
        bytes: ByteArray,
        length: Int = bytes.size
    ): Boolean

    fun toArray(start: Int = 0, length: Int = size): ByteArray
}

/**
 * A custom primitive [Byte] collection class that does not cause auto-boxing when storing and
 * retrieving elements.
 */
@Suppress("NOTHING_TO_INLINE")
class MutableByteList : ByteList, MutableList<Byte>, AbstractList<Byte> {

    @Transient
    var byteData: ByteArray

    @Transient
    private var mModificationCount = 0

    @Suppress("unused")
    constructor(bytes: ByteArray) {
        byteData = ByteArray(bytes.size)
        bytes.copyInto(
            destination = byteData,
            destinationOffset = 0,
            startIndex = 0,
            endIndex = bytes.size,
        )
        _size = byteData.size
    }

    constructor(initialCapacity: Int) {
        byteData = ByteArray(initialCapacity)
        _size = 0
    }

    internal var _size: Int = 0

    override val size: Int get() = _size

    @Suppress("unused")
    fun copyTo(data: ByteArray) = copyTo(data, 0, data.size)

    fun copyTo(dest: ByteArray, start: Int, length: Int) {
        require(start + length <= size) { "start exceeds the length of byte data" }
        require(length <= dest.size) { "The data to be copied exceeds the bounds of the buffer." }
        byteData.copyInto(
            destination = dest,
            destinationOffset = 0,
            startIndex = start,
            endIndex = (start + length),
        )
    }

    private fun rangeCheckForChecksum(fromIndex: Int, length: Int) {
        if(fromIndex >= lastIndex )
            throw IndexOutOfBoundsException("From Index: $fromIndex > lastIndex: $lastIndex or length: $length > size: $size")
    }

    @Suppress("NOTHING_TO_INLINE", "unused")
    internal inline fun calculateChecksum(fromIndex: Int = 0, length: Int = size): Byte {
        rangeCheckForChecksum(
            fromIndex = fromIndex,
            length = length,
        )

        var combinedValue: Byte = 0x00
        val end = fromIndex + length
        for (i in fromIndex until end)
            combinedValue = (combinedValue + byteData[i]).toByte()

        return combinedValue
    }

    override fun get(index: Int): Byte {
        checkIndex(index, size)
        return byteData[index]
    }

    override fun set(index: Int, element: Byte): Byte {
        checkIndex(index, size)
        // Copy the byte that is current at 'position'.
        val oldByte = byteData[index]
        // Store the b at position.
        byteData[index] = element
        return oldByte
    }

    override fun add(element: Byte): Boolean {
        mModificationCount++
        val oldSize = this.size

        if (oldSize == this.byteData.size)
            this.byteData = grow()

        this.byteData[oldSize] = element
        this._size = oldSize + 1
        return true
    }

    override fun add(index: Int, element: Byte) {
        rangeCheckForAdd(index = index)
        mModificationCount++
        val oldSize = size
        var tempArray = byteData
        if (oldSize == tempArray.size) tempArray = grow()
        tempArray.copyInto(
            destination = tempArray,
            destinationOffset = (index + 1),
            startIndex = index,
            endIndex = oldSize
        )

        tempArray[index] = element
        _size = oldSize + 1
    }

    override fun addBytes(bytes: ByteArray, length: Int): Boolean {
        mModificationCount++
        if (bytes.isEmpty()) return false

        val s = size
        val newSize = length + s
        // If the array is not big enough to add the bytes into the array, adjust the size.
        if (newSize > byteData.size) grow(newSize)
        bytes.copyInto(
            destination = byteData,
            destinationOffset = s,
            startIndex = 0,
            endIndex = length,
        )

        _size = newSize
        return true
    }

    fun addAll(
        bytes: Collection<Byte>,
        length: Int = bytes.size,
    ): Boolean = addBytes(
        bytes = bytes.toByteArray(),
        length = length,
    )

    override fun addAll(elements: Collection<Byte>): Boolean = addAll(
        bytes = elements,
        length = elements.size,
    )

    override fun addAll(index: Int, elements: Collection<Byte>): Boolean = addBytes(
        index = index,
        bytes = elements.toByteArray(),
    )

    fun addBytes(index: Int, bytes: ByteArray): Boolean {
        rangeCheckForAdd(index = index)

        val a = bytes
        mModificationCount++

        val numNew = a.size
        if (numNew == 0) return false

        var existingData = this.byteData
        val s = size

        if(numNew > (a.size - s)) {
            existingData = grow(s + numNew)
        }
        val numMoved = s - index
        if (numMoved > 0) {
            existingData.copyInto(
                destination = existingData,
                destinationOffset = index + numNew,
                startIndex = index,
                endIndex = index + numMoved,
            )
        }

        a.copyInto(
            destination = existingData,
            destinationOffset = index,
            startIndex = 0,
            endIndex = numNew,
        )

        _size = s + numNew
        return true
    }

    private fun grow(minCapacity: Int = size + 1): ByteArray {
        val oldCapacity = byteData.size
        val newCapacity = if (oldCapacity > 0) {
            maxOf(
                minCapacity,
                newLength(
                    oldCapacity,
                    minCapacity - oldCapacity, /* minimum growth */
                    (oldCapacity shr 1)           /* preferred growth */
                )
            )
        } else {
            maxOf(DEFAULT_CAPACITY, minCapacity)
        }
        return if (oldCapacity > 0) {
            byteData.copyOf(newCapacity).also { byteData = it }
        } else {
            ByteArray(newCapacity).also { byteData = it }
        }
    }

    override fun removeAt(index: Int): Byte {
        checkIndex(index, size)
        val bytes = byteData

        val oldValue = bytes[index]
        fastRemove(bytes = bytes, i = index)
        return oldValue
    }

    fun remove(index: Int): Boolean {
        removeAt(index)
        return true
    }

    private inline fun fastRemove(bytes: ByteArray, i: Int) {
        mModificationCount++
        val newSize: Int = size - 1

        if (newSize > i) {
             bytes.copyInto(
                   destination = bytes,
                   destinationOffset = i,
                   startIndex = (i + 1),
                   endIndex = size,
               )
        }

        newSize.also {
            _size = it
            bytes[it] = 0x00
        }
    }

    override fun remove(element: Byte): Boolean {
        val bytes = this.byteData
        val index = bytes.indexOf(element).also {
            if (it < 0) return@remove false
        }
        fastRemove(bytes, index)
        return true
    }

    fun removeRange(fromIndex: Int, toIndex: Int) {
        rangeCheckForRemoveRange(fromIndex, toIndex)

        mModificationCount++
        shiftTailOverGap(this.byteData, fromIndex, toIndex)
    }

    override fun removeAll(elements: Collection<Byte>): Boolean =
        batchRemove(b = elements, complement = false, from = 0, end = size)

    override fun retainAll(elements: Collection<Byte>): Boolean =
        batchRemove(b = elements, complement = true, from = 0, end = size)

    /*private fun batchRemove(
        bytes: Collection<Byte>,
        complement: Boolean,
        from: Int,
        end: Int,
    ): Boolean {
        val es = byteData
        // Optimize for initial run of survivors
        var r: Int = from
        while (true) {
            if (r == end) return false
            // Check to see if the collection contains any values that match
            if (bytes.contains(es[r]) != complement) break
            r++
        }
        var w = r++
        try {
            var e: Byte
            while (r < end) {
                val contained = bytes.contains(es[r].also { e = it })
                // If the value matches the complement, we want to retain it, otherwise skip it
                if (contained == complement) es[w++] = e
                r++
            }
        } catch (ex: Throwable) {
            // Preserve behavioral compatibility with AbstractCollection,
            // even if c.contains() throws.
        *//*    System.arraycopy(es, r, es, w, end - r)
            es.copyInto(
                destination = es,
                destinationOffset = w,
                startIndex = r,
                endIndex = end,
            )*//*
            w += end - r
            throw ex
        } finally {
            mModificationCount += end - w
            shiftTailOverGap(es, w, end)
        }
        return true
    }*/

    fun batchRemove(
        b: Collection<Byte>,
        complement: Boolean,
        from: Int, end: Int
    ): Boolean {
        val es: ByteArray = byteData
        // Optimize for initial run of survivors
        var r: Int = from
        while (true) {
            if (r == end) return false
            if (b.contains(es[r]) != complement) break
            r++
        }
        var w = r++
        try {
            var e: Byte
            while (r < end) {
                if (b.contains(es[r].also { e = it }) == complement) {
                    es[w++] = e
                }
                r++
            }
        } catch (ex: Throwable) {
            // Preserve behavioral compatibility with AbstractCollection,
            // even if c.contains() throws.
            es.copyInto(
                destination = es,
                destinationOffset = w,
                startIndex = r,
                endIndex = end
            )
            w += end - r
            throw ex
        } finally {
            mModificationCount += end - w
            shiftTailOverGap(es, w, end)
        }
        return true
    }

    private fun shiftTailOverGap(es: ByteArray, lo: Int, hi: Int) {
        es.copyInto(
            destination = es,
            destinationOffset = lo,
            startIndex = hi,
            endIndex = size,
        )
        val to: Int = size
        var i: Int = (hi - lo).let { _size -= it; _size }
        while (i < to) {
            es[i] = 0x00
            i++
        }
    }

    private fun checkForComodification(expectedModCount: Int) {
        if (mModificationCount != expectedModCount) {
            throw ConcurrentModificationException()
        }
    }

    override fun clear() {
        mModificationCount++
        for (i in 0 until size) byteData[i] = 0x00
        _size = 0
    }

    override fun iterator(): MutableIterator<Byte> = ByteListMutableIterator()

    override fun listIterator(): MutableListIterator<Byte> = ByteListMutableListIterator(index = 0)

    override fun listIterator(index: Int): MutableListIterator<Byte> =
        ByteListMutableListIterator(index = index)

    override fun subList(
        fromIndex: Int,
        toIndex: Int
    ): MutableList<Byte> = SubList(
        list = this,
        fromIndex = fromIndex,
        toIndex = toIndex,
    )

    override fun contains(element: Byte): Boolean = indexOf(element) >= 0

    override fun indexOf(element: Byte): Int = indexOfRange(byte = element, start = 0, end = size)

    @Suppress("NOTHING_TO_INLINE")
    inline fun indexOfRange(byte: Byte, start: Int, end: Int): Int {
        val bytes = byteData
        for (i in start until end) {
            if (byte == bytes[i]) return i
        }
        return -1
    }

    override fun lastIndexOf(element: Byte): Int {
        for (i in size downTo 0) {
            // We found the first index of this byte, return true.
            if (element == byteData[i]) {
                return i
            }
        }
        return -1
    }

    private fun rangeCheckForAdd(index: Int) {
        if (index > size || index < 0)
            throw IndexOutOfBoundsException("Index: $index, Size: $size")
    }

    private fun rangeCheckForRemoveRange(fromIndex: Int, toIndex: Int) {
        if (fromIndex > toIndex)
            throw IndexOutOfBoundsException("From Index: $fromIndex > To Index: $toIndex")
    }

    override fun toArray(start: Int, length: Int): ByteArray = ByteArray(length).apply {
        copyTo(dest = this@apply, start = start, length = length)
    }

    override fun toString(): String {
        if (isEmpty()) return "[]"

        val builder = StringBuilder("[")
        for (i in 0 until size) {
            if (i == size - 1) {
                builder.append(byteData[i].toInt())
            } else {
                builder.append(byteData[i].toInt()).append(",")
            }
        }
        builder.append("]")
        return builder.toString()
    }

    override fun containsAll(elements: Collection<Byte>): Boolean {
        for (b: Byte in elements)
            if (!contains(b))
                return false
        return true
    }

    open inner class ByteListMutableIterator(
        protected var cursor: Int = 0
    ) : MutableIterator<Byte> {

        protected var limit = this@MutableByteList.size
        protected var expectedModCount = this@MutableByteList.mModificationCount
        protected var lastReturnedIndex = -1 // To track the last returned element for remove()

        override fun remove() {
            if (lastReturnedIndex < 0) throw IllegalStateException()
            checkForComodification()
            try {
                this@MutableByteList.remove(lastReturnedIndex)
                cursor = lastReturnedIndex
                lastReturnedIndex = -1
                expectedModCount = this@MutableByteList.mModificationCount
                limit--
            } catch (_: IndexOutOfBoundsException) {
                throw ConcurrentModificationException()
            }
        }

        override fun hasNext(): Boolean = cursor < limit

        override fun next(): Byte {
            checkForComodification()
            val i = cursor
            if (i >= limit) throw NoSuchElementException()

            val bytes = this@MutableByteList.byteData
            if (i >= bytes.size) throw ConcurrentModificationException()

            cursor = i + 1
            return bytes[i.also { lastReturnedIndex = it }]
        }


        fun checkForComodification() {
            if (this@MutableByteList.mModificationCount != expectedModCount)
                throw ConcurrentModificationException()
        }
    }

    inner class ByteListMutableListIterator(index: Int) : ByteListMutableIterator(cursor = index),
        MutableListIterator<Byte>
    {

        override fun hasPrevious(): Boolean = cursor != 0

        override fun nextIndex(): Int = cursor

        override fun previousIndex(): Int = cursor - 1

        override fun previous(): Byte {
            checkForComodification()
            val i = cursor - 1
            if (i < 0) throw NoSuchElementException()

            val bytes = this@MutableByteList.byteData
            if (i >= bytes.size) throw ConcurrentModificationException()
            cursor = i
            return bytes[i.also { lastReturnedIndex = it }]
        }

        override fun set(element: Byte) {
            if (lastReturnedIndex < 0) throw IllegalStateException()
            checkForComodification()
            try {
                this@MutableByteList.set(lastReturnedIndex, element)
            } catch (_: IndexOutOfBoundsException) {
                throw ConcurrentModificationException()
            }
        }

        override fun add(element: Byte) {
            checkForComodification()

            try {
                val i = cursor
                this@MutableByteList.add(index = i, element = element)
                cursor = i + 1
                lastReturnedIndex = -1
                this.expectedModCount = this@MutableByteList.mModificationCount
                limit++
            } catch (_: IndexOutOfBoundsException) {
                throw ConcurrentModificationException()
            }
        }
    }

    private class SubList(
        private val list: MutableByteList,
        private val fromIndex: Int,
        private var toIndex: Int,
    ) : AbstractMutableList<Byte>()
    {
        init {
            checkRange(fromIndex, toIndex, list.size)
        }

        override val size: Int
            get() = toIndex - fromIndex

        override fun get(index: Int): Byte {
            checkElementIndex(index, size)
            return list[fromIndex + index]
        }

        override fun add(index: Int, element: Byte) {
            checkPositionIndex(index, size)
            list.add(fromIndex + index, element)
            toIndex++
        }

        override fun removeAt(index: Int): Byte {
            checkElementIndex(index, size)
            val removedElement = list.removeAt(fromIndex + index)
            toIndex--
            return removedElement
        }

        override fun set(index: Int, element: Byte): Byte {
            checkElementIndex(index, size)
            return list.set(fromIndex + index, element)
        }

        override fun iterator(): MutableIterator<Byte> {
            return SubListIterator(this)
        }

        private fun checkRange(fromIndex: Int, toIndex: Int, size: Int) {
            if (fromIndex < 0 || toIndex > size || fromIndex > toIndex) {
                throw IndexOutOfBoundsException("fromIndex: $fromIndex, toIndex: $toIndex, size: $size")
            }
        }

        private fun checkElementIndex(index: Int, size: Int) {
            if (index < 0 || index >= size) {
                throw IndexOutOfBoundsException("index: $index, size: $size")
            }
        }

        private fun checkPositionIndex(index: Int, size: Int) {
            if (index < 0 || index > size) {
                throw IndexOutOfBoundsException("index: $index, size: $size")
            }
        }

        private inner class SubListIterator(private val subList: SubList) : MutableListIterator<Byte> {
            private var cursor = 0
            private var lastReturned = -1

            override fun hasNext(): Boolean = cursor < subList.size

            override fun next(): Byte {
                if (!hasNext()) throw NoSuchElementException()
                lastReturned = cursor
                return subList[cursor++]
            }

            override fun hasPrevious(): Boolean = cursor > 0

            override fun previous(): Byte {
                if (!hasPrevious()) throw NoSuchElementException()
                lastReturned = --cursor
                return subList[cursor]
            }

            override fun nextIndex(): Int = cursor

            override fun previousIndex(): Int = cursor - 1

            override fun remove() {
                if (lastReturned < 0) throw IllegalStateException()
                subList.removeAt(lastReturned)
                if (lastReturned < cursor) {
                    cursor--
                }
                lastReturned = -1
            }

            override fun set(element: Byte) {
                if (lastReturned < 0) throw IllegalStateException()
                subList[lastReturned] = element
            }

            override fun add(element: Byte) {
                subList.add(cursor++, element)
                lastReturned = -1
            }
        }
    }

    override fun hashCode(): Int {
        super.hashCode()
        var result = super.hashCode()
        result = 31 * result + byteData.contentHashCode()
        result = 31 * result + mModificationCount
        result = 31 * result + _size
        result = 31 * result + size
        return result
    }

    override fun equals(other: Any?): Boolean {
        if(other == this) return true

        if(other !is MutableByteList) return false

        if(other.size != size) return false

        val expectedModCount = mModificationCount
        for(i in 0 until size) {
            if(byteData[i] != other.byteData[i]) return false
        }
        checkForComodification(expectedModCount)
        return true
    }

    companion object {
        /**
         * Default capacity is longest possible [io.github.developrofthings.kespl.packet.ESPPacket] that can fit within
         * an Bluetooth LE data gram.
         */
        const val DEFAULT_CAPACITY = 20
    }
}

internal fun checkIndex(index: Int, size: Int) {
    if(index >= size) throw IndexOutOfBoundsException("Index: $index, Size: $size")
}