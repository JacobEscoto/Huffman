import java.util.PriorityQueue;

public class Huffman {

  // Saves how many times does a byte with value `x` appears and returns an array of 256 positions
  public static int[] countFrequencies(byte[] data) {
    int[] frequencies = new int[256];

    for (byte dat : data) {
      int symbol = dat & 0xFF;
      frequencies[symbol]++;
    }

    return frequencies;
  }

  // Builds the Huffman tree
  public static Node buildTree(int[] frequencies) {
    PriorityQueue<Node> queue = new PriorityQueue<>((a, b) -> Node.compare(a, b));
    int nextOrder = 0;

    for (int symbol = 0; symbol < 256; symbol++) {
      if (frequencies[symbol] > 0) {
        Node leaf = Node.newLeaf((byte) symbol, frequencies[symbol], nextOrder);

        queue.add(leaf);
        nextOrder++;
      }
    }
    if (queue.isEmpty()) {
      return null;
    }

    while (queue.size() > 1) {
      Node left = queue.poll();
      Node right = queue.poll();

      Node parent = Node.newIntern(left, right, nextOrder);

      queue.add(parent);
      nextOrder++;
    }
    return queue.poll();
  }

  // walks through the Huffman tree from the root and assigns every leaf a code
  public static String[] generateCodes(Node root) {
    String[] table = new String[256];

    if (root != null) {
      generateCodes(root, "", table);
    }

    return table;
  }

  private static void generateCodes(Node node, String prefix, String[] table) {
    if (node.isLeaf()) {
      if (prefix.isEmpty()) {
        prefix = "0";
      }

      int indiceSimbol = node.symbol() & 0xFF;
      table[indiceSimbol] = prefix;
      return;
    }

    generateCodes(node.left(), prefix + "0", table);
    generateCodes(node.right(), prefix + "1", table);
  }
}
