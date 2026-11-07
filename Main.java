import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Main {
  private static final int SYMBOL_COUNT = 256;
  private static final char SPACE_DISPLAY = '␣';

  public static void main(String[] args) {
    if (args.length < 2) {
      printHelp();
      System.exit(1);
    }

    String flag = args[0];
    final boolean hasTwoArgsToEncode = flag.equals("-encode") && args.length == 2;
    final boolean hasThreeArgsToDecode = flag.equals("-decode") && args.length == 3;

    switch (flag) {
      case "-encode" -> {
        if (!hasTwoArgsToEncode) {
          System.out.println("[ERROR] Debes encerrar el texto entre comillas.");
          System.out.println("  java Main -encode \"TEXTO\"");
          System.exit(1);
        }
        String text = args[1];
        encode(text);
      }
      case "-decode" -> {
        if (!hasThreeArgsToDecode) {
          System.out.println(
              "[ERROR] Debes incluir la tabla de frecuencias y una cadena de bits para mostrar el"
                  + " texto decodificado.");
          System.out.println("  java Main -decode \"TABLA\" \"CADENA DE BITS\"");
          System.exit(1);
        }
        String plainTable = args[1];
        String bitsChain = args[2];

        decode(extractFrequenciesTable(plainTable), bitsChain);
      }
      default -> {
        System.out.println("\nFlag desconocido o no válido.");
        printHelp();
        System.exit(1);
      }
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
    if (text == null || text.isEmpty()) {
      System.out.println("El texto está vacío. Ingresar un texto válido.");
      System.exit(1);
    }
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

  // Converts plain text into a frequencies table
  private static String[] extractFrequenciesTable(String plainText) {
    if (plainText == null || plainText.isEmpty()) {
      System.out.println("La tabla está vacía");
      System.exit(1);
    }

    String[] parts = plainText.split(",");
    String[] table = new String[parts.length];

    for (int i = 0; i < parts.length; i++) {
      String entry = parts[i].trim();
      if (entry.isEmpty()) {
        System.out.println("Hay una entrada vacía en la tabla");
        System.exit(1);
      }

      String[] fields = entry.split(":");
      if (fields.length != 2 || fields[0].isEmpty() || fields[1].isEmpty()) {
        System.out.println("Entrada no válida: " + entry);
        System.exit(1);
      }

      table[i] = entry;
    }

    return table;
  }

  private static void decode(String[] table, String bits) {
    if (bits == null || bits.isEmpty()) {
      System.out.println("La cadena de bits está vacía");
      System.exit(1);
    }

    int[] frequencies = new int[SYMBOL_COUNT];

    for (String entry : table) {
      String symbolStr = entry.substring(0, entry.indexOf(":"));
      if (symbolStr.equals("_")) symbolStr = " ";
      if (symbolStr.length() != 1) {
        System.out.println("El símbolo debe tener solamente un carácter");
        System.exit(1);
      }
      char symbol = symbolStr.charAt(0);
      if (symbol > 255) {
        System.out.println("El símbolo " + symbol + " no está permitido");
        System.exit(1);
      }
      String frequencyStr = entry.substring(entry.indexOf(":") + 1);
      int frequency = 0;
      try {
        frequency = Integer.parseInt(frequencyStr);
      } catch (NumberFormatException ex) {
        System.out.println("La Frecuencia de la tabla no es un número válido");
        System.exit(1);
      }
      if (frequency <= 0) {
        System.out.println("La frecuencia de " + symbol + " no puede ser menor o igual a 0");
        System.exit(1);
      }
      if (frequencies[symbol] > 0) {
        System.out.println("El símbolo está repetido");
        System.exit(1);
      }

      frequencies[symbol] = frequency;
    }

    Node root = Huffman.buildTree(frequencies);

    bits = bits.replace(" ", "");
    for (char character : bits.toCharArray()) {
      if (character != '0' && character != '1') {
        System.out.println("Cadena de bits no válida, solo se acepta 0 y 1");
        System.exit(1);
      }
    }

    if (root == null) {
      System.out.println("La raíz es nula");
      System.exit(1);
    }

    if (root.isLeaf()) {
      String message = "";
      for (int i = 0; i < bits.length(); i++) message += (char) (root.symbol() & 0xFF);
      printTitle("Texto decodificado");
      System.out.println(message);
      return;
    }

    Node current = root;
    String message = "";

    for (char bit : bits.toCharArray()) {
      if (bit == '0') {
        current = current.left();
      } else {
        current = current.right();
      }
      if (current.isLeaf()) {
        message += (char) (current.symbol() & 0xFF);
        current = root;
      }
    }
    if (current != root) {
      System.out.println("La cadena está incompleta");
      System.exit(1);
    }

    int totalFrequency = 0;
    for (int frequency : frequencies) totalFrequency += frequency;
    if (message.length() != totalFrequency) {
      System.out.println("El mensaje decodificado no tiene la longitud esperada");
      System.exit(1);
    }

    printTitle("Texto decodificado");
    System.out.println(message);
  }

  // Returns a list just of symbols with a frequency greater than zero
  private static List<Integer> activeSymbols(int[] frequencies) {
    List<Integer> symbols = new ArrayList<>();
    for (int i = 0; i < SYMBOL_COUNT; i++) {
      if (frequencies[i] > 0) symbols.add(i);
    }
    return symbols;
  }

  private static String displayChar(int symbol) {
    char character = (char) symbol;
    return String.valueOf(character == ' ' ? SPACE_DISPLAY : character);
  }

  private static String label(Node node) {
    return notation(node) + "(" + node.frequency() + ")";
  }

  private static String notation(Node node) {
    if (node.isLeaf()) return displayChar(node.symbol() & 0xFF);
    return "(" + notation(node.left()) + notation(node.right()) + ")";
  }

  private static void printTitle(String title) {
    System.out.printf("%n## %s%n", title);
    printSeparator(35);
    System.out.println();
  }

  private static void printSeparator(int width) {
    System.out.println("-".repeat(width));
  }

  private static void printFrequencyTable(int[] frequencies) {
    printTitle("Tabla de Frecuencias");
    System.out.println("| Caracter | Frecuencia | Núm. de llegada |");
    printSeparator(44);

    int arrival = 1;
    for (int i : activeSymbols(frequencies)) {
      System.out.printf("| %-8s | %-10d | %-15d |%n", displayChar(i), frequencies[i], arrival++);
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
          "%d. Nodos extraídos: %s y %s  -->  Nuevo nodo: %s%n",
          steps++, label(node.left()), label(node.right()), label(node));
    }
  }

  private static int printCodeTable(int[] frequencies, String[] table) {
    printTitle("Tabla de Códigos");
    System.out.println("| Símbolo | Frecuencia | Código | Longitud | Bits |");
    printSeparator(53);

    int totalBits = 0;
    int totalFrequency = 0;
    for (int i : activeSymbols(frequencies)) {
      String code = table[i];
      int symbolBits = frequencies[i] * code.length();

      System.out.printf(
          "| %-7s | %-10d | %-6s | %-8d | %d%n",
          displayChar(i), frequencies[i], code, code.length(), symbolBits);

      totalBits += symbolBits;
      totalFrequency += frequencies[i];
    }
    System.out.printf("Frecuencia Total: %d%n", totalFrequency);
    System.out.printf("Total Bits: %d%n", totalBits);
    return totalBits;
  }

  private static void printEncodedText(String text, String[] table) {
    printTitle("Texto codificado");

    String[] codes = new String[text.length()];
    for (int i = 0; i < text.length(); i++) codes[i] = table[text.charAt(i) & 0xFF];
    System.out.println(String.join(" ", codes));
  }

  private static void printMetrics(int totalBits, int symbolsAmount, int textLength) {

    int bitsPerSymbol = 0;
    while ((1 << bitsPerSymbol) < symbolsAmount) bitsPerSymbol++;

    int fixedBits = bitsPerSymbol * textLength;
    int bitsAscii = 8 * textLength;
    double ratio = (double) totalBits / bitsAscii;
    double savings = 1 - ratio;
    double savingsPercentage = savings * 100;
    double average = (double) totalBits / textLength;

    int header = 4 + 8 + 2 + (5 * symbolsAmount);
    int dataBytes = (int) Math.ceil(totalBits / 8.0);
    int realSize = header + dataBytes;

    printTitle("Métricas");
    System.out.printf("Bits de Huffman: %d%n", totalBits);
    System.out.printf("Código fijo: %d × %d = %d%n", bitsPerSymbol, textLength, fixedBits);
    System.out.printf("ASCII: 8 × %d = %d%n", textLength, bitsAscii);
    System.out.printf("Razón de compresión: %d ÷ %d = %.3f%n", totalBits, bitsAscii, ratio);
    System.out.printf("Ahorro: 1 - %.3f = %.3f ≈ %.1f%s%n", ratio, savings, savingsPercentage, "%");
    System.out.printf(
        "Bits promedio por símbolo: %d ÷ %d = %.2f%n", totalBits, textLength, average);
    System.out.println("Tamaño real del archivo comprimido:");
    System.out.printf("  - Encabezado: %d%n", header);
    System.out.printf("  - Datos: [%d ÷ 8] = %d%n", totalBits, dataBytes);
    System.out.printf("  - Total: %d + %d = %d bytes%n", header, dataBytes, realSize);

    boolean isGreater = realSize > textLength;
    System.out.printf(
        "  - El texto comprimido es %s que el texto original de %d bytes%n",
        isGreater ? "mayor" : "menor", textLength);
  }

  private static void collectInternal(Node node, List<Node> nodes) {
    if (node == null || node.isLeaf()) return;
    nodes.add(node);
    collectInternal(node.left(), nodes);
    collectInternal(node.right(), nodes);
  }
}
