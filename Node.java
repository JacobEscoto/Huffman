record Node(byte symbol, int frequency, Node left, Node right, int order) {

  public static Node newLeaf(byte symbol, int frequency, int order) {
    return new Node(symbol, frequency, null, null, order);
  }

  public static Node newInternal(Node x, Node y, int order) {
    return new Node((byte) 0, x.frequency() + y.frequency(), x, y, order);
  }

  public boolean isLeaf() {
    return left == null && right == null;
  }

  public static boolean minor(Node firstNode, Node secondNode) {
    if (firstNode.frequency() != secondNode.frequency()) {
      return firstNode.frequency() < secondNode.frequency();
    }
    return firstNode.order() < secondNode.order();
  }
}
