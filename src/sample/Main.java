package sample;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main extends Application {

    private int max_disk_num = 7;  //最多支持的圆盘个数

    /*这个数由用户输入*/
    private int disk_num;  //圆盘的个数

    private int divide_width = 300;    //每个柱子要占用的面积的大小，用来计算圆盘的移动

    private int baseline = 960; //放置圆盘的底线

    private int height = 20;   //圆盘的高度

    /*这个数会改变*/
    private int[] position_num = {disk_num, 0, 0};  //每个柱子上面有几个圆盘

    private int[] x_num = {100, 85, 70, 55, 40, 25, 10};  //绘画圆盘的x坐标

    private int[] y_num = {820, 840, 860, 880, 900, 920, 940};  //绘画圆盘的y坐标

    private int[] width_num = {100, 130, 160, 190, 220, 250, 280};   //每个圆盘的宽度

    int A = 0;  //第一个柱子，以数字0表示为了方便计算
    int B = 1;  //第二个柱子，以数字0表示为了方便计算
    int C = 2;  //第三个柱子，以数字0表示为了方便计算

    List<Color> colors = new ArrayList<>();      //缓存颜色值，红橙黄绿蓝紫緑  七种
    List<Rectangle> rects = new ArrayList<>();   //缓存应该画出的圆盘，最多七个
    List<Button> stackNode = new ArrayList<>();  //缓存堆栈View中的每个节点

    @FXML
    private Button button;  //应用启动按钮

    @FXML
    private TextField input;  //有用户输入，确定演示几阶汉诺塔

    @FXML
    private Label tipText;   //对用户进行提示

    @FXML
    private Label warningText;  //对用户的错误行为进行提醒

    @FXML
    private AnchorPane root;  //根布局

    @FXML
    private GridPane stackPane;  //堆栈布局

    @FXML
    private Label tipStackText;  //堆栈解释

    @FXML
    private ImageView imageView;  //展示核心程序图片

    @FXML
    private Label timeClock;  //展示用去的时间

    private static boolean isOver;  //判断程序是否结束

    private static int stackBodeCount = 0;

    @Override
    public void start(Stage primaryStage) throws IOException {

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(Main.class.getResource("rootBackground.fxml"));
        root = (AnchorPane) loader.load();

        primaryStage.setTitle("汉诺塔算法演示");

        Scene scene = new Scene(root, 1900, 1000);

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * 对控件进行初始化
     */
    @FXML
    private void initialize() {
        tipText.setText("因为屏幕大小和演示时间的原因，请输入小于等于7的正整数");
        tipStackText.setText("move(n, from, to)\nn: 圆盘的编号\nfrom: 起始柱子\nto: 目标柱子 \n\n" +
                "hanoi(n, from, assist, to)\nn:圆盘的编号\nfrom: 起始柱子\nassist: 辅助柱子\nto: 目标柱子");

        Image image = new Image("file:image.png", true);
        imageView = new ImageView(image);
    }

    /**
     * 对启动按钮的监听
     */
    @FXML
    private void startArith() {

        stackBodeCount = 0;

        /*每次开始之前，对屏幕进行清空*/
        root.getChildren().removeAll(rects);
        rects.clear();

        /*错误行为的提示言语*/
        String errorMessage = "";

        initColors();

        /*确定是否输入*/
        if (input.getText() == null || input.getText().length() == 0) {
            errorMessage += "输入无效！\n";
            warningText.setText(errorMessage);
            input.setText("");
            return;
        } else {
            /*确定是否正整数*/
            try {
                Integer.parseInt(input.getText());
            } catch (NumberFormatException e) {
                errorMessage += "输入的数值无效，必须是正整数!\n";
                warningText.setText(errorMessage);
                input.setText("");
                return;
            }
        }

        disk_num = Integer.parseInt(input.getText());

        /*确定是否小于等于7*/
        if (disk_num > 7) {
            errorMessage += "输入的数值无效，请输入小于等于7的正整数!\n";
            warningText.setText(errorMessage);
            input.setText("");
            return;
        }

        isOver = false;


        //显示时钟
        new Thread(() -> {
            int min = 0;  //分
            int ten_sec = 0;  // 十位的秒
            int sec = 0;  //个位的秒
            while (!isOver) {
                String time = "0" + min + ":" + ten_sec + sec;  //组合时间
                Platform.runLater(() -> timeClock.setText(time));  //在 applacation 线程更改UI
                try {
                    Thread.sleep(990);  //当前线程睡眠1秒
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                //对时间的变换进行判断
                sec++;
                if (sec > 9){
                    ten_sec++;
                    sec = 0;
                }

                if (ten_sec > 5){
                    min++;
                    ten_sec = 0;
                }
            }
        }).start();

        /*将每个柱子上的圆盘数进行初始化*/
        initPositionNum();

        initRects(disk_num);

        new Thread(() -> {
            /*设置按钮不可点击*/
            button.setDisable(true);
            hanoi(disk_num-1, A, B, C);
            /*恢复按钮*/
            button.setDisable(false);
            isOver = true;
        }).start();
    }

    /**
     * 初始化每个柱子上的圆盘数
     */
    private void initPositionNum() {
        position_num[0] = disk_num;
        position_num[1] = 0;
        position_num[2] = 0;
    }

    /**
     * 初始化盘子的颜色
     */
    private void initColors() {
        colors.add(Color.BLACK);
        colors.add(Color.PURPLE);
        colors.add(Color.BLUE);
        colors.add(Color.GREEN);
        colors.add(Color.YELLOW);
        colors.add(Color.ORANGE);
        colors.add(Color.RED);
    }

    /**
     * 初始化盘子的个数
     * @param rectNum
     */
    private void initRects(int rectNum) {
        for (int i = 0; i < rectNum; i++) {
            final Rectangle rect = new Rectangle(x_num[i+(max_disk_num-rectNum)],
                    y_num[i+(max_disk_num-rectNum)],
                    width_num[i+(max_disk_num-rectNum)],
                    height);
            rect.setArcWidth(15);
            rect.setArcHeight(15);
            rect.setFill(colors.get(i+(max_disk_num-rectNum)));
            rects.add(rect);
            root.getChildren().add(rect);
        }
    }

    /**
     * 移动盘子
     * @param from 起始柱子
     * @param to   目标柱子
     * @param rect 被移动的圆盘
     */
    private void moveDisk(int from, int to, Rectangle rect) {

        /*第一步；将圆盘向上移动*/
        final Timeline timeline=new Timeline();  //创建时间轴
        final KeyValue kv_1 = new KeyValue(rect.yProperty(), (baseline - (height*(disk_num+2))));  //设置操作
        final KeyFrame kf_1 = new KeyFrame(Duration.millis(900), kv_1);  //设置关键帧

        //将关键帧加到时间轴中
        timeline.getKeyFrames().add(kf_1);
        timeline.play();//运行动画

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        /*第二步：将圆盘移动到目标柱子的上面*/
        final Timeline timeline_2 = new Timeline();
        final KeyValue kv_2 = new KeyValue(rect.xProperty(), (to-from)*divide_width + rect.getX());
        final KeyFrame kf_2 = new KeyFrame(Duration.millis(1500), kv_2);
        timeline_2.getKeyFrames().add(kf_2);
        timeline_2.play();//运行

        try {
            Thread.sleep(1600);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        /*第三步：将圆盘放在柱子的合适位置*/
        final Timeline timeline_3 = new Timeline();
        final KeyValue kv_3 = new KeyValue(rect.yProperty(), baseline - (position_num[to] + 1) * height);
        final KeyFrame kf_3 = new KeyFrame(Duration.millis(900), kv_3);
        timeline_3.getKeyFrames().add(kf_3);
        timeline_3.play();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        /*对每个柱子上面的圆盘数进行修改*/
        position_num[from]--;
        position_num[to]++;

    }

    public static void main(String[] args) {
        launch(args);
    }

    /*对柱子进行移动*/
    private void move(int disks, int N, int M) {
        moveDisk(N, M, rects.get(disks));
    }

    /**
     * 汉诺塔递归算法
     * @param n 圆盘编号
     * @param A 开始的柱子
     * @param B 辅助柱子
     * @param C 目标柱子
     */
    private void hanoi(int n, int A, int B, int C) {
        if (n == 0) {

            addStackNode(n, A, C);

            move(n, A, C);

            removeStackNode();

        } else {
            addStackNode(n, A, B, C);

            hanoi(n-1, A, C, B);

            removeStackNode();

            addStackNode(n, A, C);

            move(n, A, C);

            removeStackNode();

            addStackNode(n, A, B, C);

            hanoi(n-1, B, A, C);

            removeStackNode();
        }
    }

    /**
     * 在堆栈演示中使用，移除已经出栈的方法节点
     */
    private void removeStackNode() {
        Platform.runLater(() -> {
            stackNode.get(stackNode.size() - 1).setVisible(false);
            stackNode.remove(stackNode.size()-1);
            stackBodeCount --;
        });
    }

    /**
     * 添加要展示的入栈的方法节点
     * @param n 圆盘编号
     * @param A 开始的柱子
     * @param B 辅助柱子
     * @param C 目标柱子
     */
    private void addStackNode(int n, int A, int B, int C) {
        Platform.runLater(() -> {
            Button btn = new Button("hanoi(" + n + ", " + A + ", " + C + ", " + B + ")");
            btn.setMinWidth(180);
            btn.setMinHeight(40);
            btn.setFont(new Font(18));
            stackNode.add(btn);
            stackPane.add(stackNode.get(stackNode.size() - 1), 0, 9-stackBodeCount);
            stackBodeCount++;
        });
    }

    private void addStackNode(int n, int A, int C) {
        Platform.runLater(() -> {
            Button btn = new Button("move(" + n + ", " + A + ", " + C +")");
            btn.setMinWidth(180);
            btn.setMinHeight(40);
            btn.setFont(new Font(18));
            stackNode.add(btn);
            stackPane.add(stackNode.get(stackNode.size() - 1), 0, 9-stackBodeCount);
            stackBodeCount++;
        });
    }
}