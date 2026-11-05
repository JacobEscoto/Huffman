/*
 * A node saves its children, the frequency sum, and its symbol.
 *    - symbol would be null if a Node is not a leaf.
 *    - left & right would be null when the node is a leaf.
 *    - order represents the number of arrival into the queue.
 */
record Node(Byte symbol, int frequency, Node left, Node right, int order) {

  public static Node newLeaf(byte symbol, int frequency, int order) {
    return new Node(symbol, frequency, null, null, order);
  }

  public static Node newIntern(Node left, Node right, int order) {
    return new Node(null, left.frequency() + right.frequency(), left, right, order);
  }

  public boolean isLeaf() {
    return left == null && right == null;
  }

  public static boolean minor(Node firstNode, Node secondNode) {
    if (firstNode.frequency() != secondNode.frequency())
      return firstNode.frequency() < secondNode.frequency();

    return firstNode.order() < secondNode.order();
  }

  public static int compare(Node firstNode, Node secondNode) {
    if (Node.minor(firstNode, secondNode)) return -1;
    if (Node.minor(secondNode, firstNode)) return 1;
    return 0;
  }
}
