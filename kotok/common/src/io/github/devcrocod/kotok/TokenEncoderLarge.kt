package io.github.devcrocod.kotok

import io.github.devcrocod.kotok.api.TokenArray



public object TokenEncoderLarge {

    internal fun calculateTokensLarge(
        tokenEncoder: TokenEncoder,
        maxTokenCount: Int,
        keepEncodings: Boolean,
        out: TokenArray,
        match: ByteArrayWrapper
    ): Int {
        val rankMap = mutableMapOf<Int, MutableMap<Int, RankNode>>()

        var prev: RankNode? = null

        for (i in 0..match.size) {
            val rank = tokenEncoder.encode(match, i, i + 2)
            val node = RankNode(rank, i, prev)
            prev?.let { it.next = node }
            prev = node

            rankMap.getOrPut(rank) { LinkedHashMap() }[i] = node
        }
        require(rankMap.containsKey(TokenEncoder.MAX_RANK))

        var tokenCount = match.size
        while (tokenCount > 2 && rankMap.size > 1) {
            val firstEntry = rankMap.entries.first()
            val it = firstEntry.value.values.iterator()

            while (it.hasNext()) {
                val minNode = it.next()
                val minRank = minNode.rank
                require(minRank != TokenEncoder.MAX_RANK)

                val previousNode = minNode.prev
                val nextNode = minNode.next
                val nextNextNode = nextNode?.next
                val nextNextNextNode = nextNextNode?.next

                if (previousNode != null) {
                    val newRank = tokenEncoder.encode(match, previousNode.index, nextNextNode?.index ?: Int.MAX_VALUE)
                    if (previousNode.rank != newRank) {
                        require(previousNode.rank != minRank)
                        removeNode(rankMap[previousNode.rank], rankMap, previousNode)
                        previousNode.rank = newRank
                        rankMap.getOrPut(newRank) { LinkedHashMap() }[previousNode.index] = previousNode
                    }
                }

                val newRank = tokenEncoder.encode(match, minNode.index, nextNextNextNode?.index ?: Int.MAX_VALUE)
                minNode.rank = newRank
                rankMap.getOrPut(newRank) { LinkedHashMap() }[minNode.index] = minNode

                minNode.next = nextNextNode
                nextNextNode?.prev = minNode
                if (nextNode?.rank != TokenEncoder.MAX_RANK) {
                    if (nextNode?.rank != minRank) {
                        removeNode(rankMap[nextNode?.rank], rankMap, nextNode!!)
                    } else {
                        it.next()
                    }
                }

                tokenCount--
            }
            rankMap.remove(firstEntry.key)
        }

        if (keepEncodings) {
            var head = rankMap[TokenEncoder.MAX_RANK]?.get(0)
            while (head?.next != null && out.size < maxTokenCount) {
                val token = tokenEncoder.encode(match, head.index, head.next!!.index)
                require(token != TokenEncoder.MAX_RANK) { "Token should not be MAX_RANK" }
                out.add(token)
                head = head.next
            }
        }

        return tokenCount
    }

    private fun removeNode(
        nodeMap: MutableMap<Int, RankNode>?,
        rankMap: MutableMap<Int, out MutableMap<Int, RankNode>>,
        node: RankNode
    ) {
        requireNotNull(nodeMap)
        if (nodeMap.size == 1) {
            require(nodeMap.containsKey(node.index))
            rankMap.remove(node.rank)
        } else {
            nodeMap.remove(node.index)
        }
    }

    private class RankNode(var rank: Int, val index: Int, var prev: RankNode?) {
        var next: RankNode? = null

        override fun toString(): String {
            return "RankNode{rank=$rank, index=$index}"
        }
    }
}
