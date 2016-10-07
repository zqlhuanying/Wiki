package classic;

/**
 * Created by 14160 on 2016/10/7.
 */
// 求二叉树的高度（多叉树类同）
public class TreeHeight {
    public static void main(String[] args) {
        Node root = new Node(1);
        Node left = new Node(2);
        Node right = new Node(2);
        root.setLeft(left);
        left.setRight(right);
        System.out.println(getHeight(root));
    }

    public static int getHeight(Node root) {
        int leftHeight = 0;
        int rightHeight = 0;
        int height = 0;
        if (root == null) {
            return 0;
        }
        leftHeight = getHeight(root.getLeft());
        rightHeight = getHeight(root.getRight());
        height = Math.max(leftHeight, rightHeight) + 1;
        return height;
    }

    static class Node {
        private Node left;
        private Node right;
        private int data;

        public Node(int data) {
            this.data = data;
        }

        public Node getLeft() {
            return left;
        }

        public void setLeft(Node left) {
            this.left = left;
        }

        public Node getRight() {
            return right;
        }

        public void setRight(Node right) {
            this.right = right;
        }

        public int getData() {
            return data;
        }

        public void setData(int data) {
            this.data = data;
        }
    }
}
