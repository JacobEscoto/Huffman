import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Main {
  private static final int SYMBOL_COUNT = 256;
  private static final char SPACE_DISPLAY = '␣';

  public static void main(String[] args) {

    if (args.length > 1) {
      switch (args[0]) {
        case "-encode":
          encode(args[1]);
          break;
        case "-decode":
          System.out.println("decoding text...");
          break;
        default:
          System.out.println();
          System.out.println("Flag desconocido.");
          printHelp();
          System.exit(1);
          break;
      }
    } else {
      printHelp();
      System.exit(1);
    }
  }

  private static void printHelp() {
    System.out.println("Uso: java Main [FLAGS] \"TEXTO\"");
    System.out.println("\nFLAGS:");
    System.out.printf(
        "  -encode        Comprimir texto con su tabla de frecuencias, pasos y texto codificado%n");
    System.out.printf("  -decode        Recrear texto original del texto comprimido%n");
  }

  private static void encode(String text) {
    int[] frequencies = Huffman.countFrequencies(text.getBytes(StandardCharsets.US_ASCII));

    printFrequencyTable(frequencies);

    Node root = Huffman.buildTree(frequencies);
    String[] table = Huffman.generateCodes(root);

    int symbolsAmount = activeSymbols(frequencies).size();
    int textLength = text.length();

    printMergesTable(root);
    int totalBits = printCodeTable(frequencies, table);
    printEncodedText(text, table);
    printMetrics(totalBits, symbolsAmount, textLength);
  }

  private static List<Integer> activeSymbols(int[] frequencies) {
    List<Integer> symbols = new ArrayList<>();
    for (int i = 0; i < SYMBOL_COUNT; i++) {
      if (frequencies[i] > 0) symbols.add(i);
    }
    return symbols;
  }

  private static String displayChar(int symbol) {
    char c = (char) symbol;
    return String.valueOf(c == ' ' ? SPACE_DISPLAY : c);
  }

  private static String label(Node node) {
    return notation(node) + "(" + node.frequency() + ")";
  }

  private static String notation(Node node) {
    if (node.isLeaf()) return displayChar(node.symbol() & 0xFF);
    return "(" + notation(node.left()) + notation(node.right()) + ")";
  }

  private static void printTitle(String title) {
    System.out.printf("%n=== %s ===%n", title);
  }

  private static void printSeparator(int width) {
    System.out.println("-".repeat(width));
  }

  private static void printFrequencyTable(int[] frequencies) {
    printTitle("Tabla de Frecuencias");
    System.out.println("| Caracter | Frecuencia | Num. de llegada |");
    printSeparator(45);

    int arrival = 1;
    for (int idx : activeSymbols(frequencies)) {
      System.out.printf(
          "| %-8s | %-10d | %-15d |%n", displayChar(idx), frequencies[idx], arrival++);
    }
  }

  private static void printMergesTable(Node root) {
    printTitle("Tabla de Pasos");

    List<Node> nodes = new ArrayList<>();
    collectInternal(root, nodes);
    nodes.sort(Comparator.comparingInt(Node::order));

    int steps = 1;
    for (Node node : nodes) {
      System.out.printf(
          "Paso %d | Se extraen: %s y %s | Nuevo nodo: %s%n",
          steps++, label(node.left()), label(node.right()), label(node));
    }
  }

  private static int printCodeTable(int[] frequencies, String[] table) {
    printTitle("Tabla de Códigos");
    System.out.println("| Simbolo | Frecuencia | Codigo | Longitud | Bits |");
    printSeparator(53);

    int totalBits = 0;
    int totalFrequency = 0;
    for (int idx : activeSymbols(frequencies)) {
      String code = table[idx];
      int symbolBits = frequencies[idx] * code.length();

      System.out.printf(
          "| %-7s | %-10d | %-6s | %-8d | %d%n",
          displayChar(idx), frequencies[idx], code, code.length(), symbolBits);

      totalBits += symbolBits;
      totalFrequency += frequencies[idx];
    }
    System.out.printf("Frecuencia Total: %d%n", totalFrequency);
    System.out.printf("Total Bits: %d%n", totalBits);
    return totalBits;
  }

  private static void printEncodedText(String text, String[] table) {
    printTitle("Texto codificado");

    String[] codes = new String[text.length()];
    for (int idx = 0; idx < text.length(); idx++) codes[idx] = table[text.charAt(idx) & 0xFF];
    System.out.println(String.join(" ", codes));
  }

  private static void printMetrics(int totalBits, int symbolsAmount, int textLength) {
    int fixedBits = (int) Math.ceil(Math.log(symbolsAmount) / Math.log(2)) * textLength;
    int bitsAscii = 8 * textLength;
    double ratio = (double) totalBits / bitsAscii;
    double savings = 1 - ratio;
    double savingsPercentage = savings * 100;
    double average = (double) totalBits / textLength;

    int header = 4 + 8 + 2 + (5 * symbolsAmount);
    int dataBytes = (int) Math.ceil(totalBits / 8.0);

    printTitle("Métricas");
    System.out.printf("Bits de Huffman: %d%n", totalBits);
    System.out.printf("Código fijo: %d%n", fixedBits);
    System.out.printf("ASCII: %d%n", bitsAscii);
    System.out.printf("Razón de compresión: %.3f%n", ratio);
    System.out.printf("Ahorro: 1 - %.3f = %.3f, %.1f%s%n", ratio, savings, savingsPercentage, "%");
    System.out.printf("Bits promedio por simbolo: %.2f%n", average);
    System.out.println("Tamaño real del archivo comprimido:");
    System.out.printf("  - Ecabezado: %d%n", header);
    System.out.printf("  - Datos: [%d / 8] = %d%n", totalBits, dataBytes);
    System.out.printf("  - Total: %d + %d = %d bytes%n", header, dataBytes, (header + dataBytes));
  }

  private static void collectInternal(Node node, List<Node> nodes) {
    if (node == null || node.isLeaf()) return;
    nodes.add(node);
    collectInternal(node.left(), nodes);
    collectInternal(node.right(), nodes);
  }
}
