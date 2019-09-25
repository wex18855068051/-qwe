package com.jikexueyuan.game2048;

import java.util.ArrayList;
import java.util.List;

import android.R.bool;
import android.R.id;
import android.R.xml;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Point;
import android.provider.ContactsContract.CommonDataKinds.Event;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ListView;

public class GameView extends GridLayout { // 2048游戏主类 让这个类绑定xml

	private Card[][] cardsMap = new Card[4][4]; // 用一个二维数组来记录下方阵
	private List<Point> emptyPoints = new ArrayList<Point>(); // 把所有的位置（空点的位置）全放在一个数组里面，方便随机地去取

	// 为了能让这个GameView从xml文件中能够访问到，要添加构造方法（能传入相关属性的构造方法），为了保险起见，最好把它的三个构造方法都添加上
	public GameView(Context context, AttributeSet attrs, int defStyle) { // 构造函数
		super(context, attrs, defStyle);
		initGameView(); // 初始化
	}

	public GameView(Context context) { // 构造函数
		super(context);
		initGameView();
	}

	public GameView(Context context, AttributeSet attrs) { // 构造函数
		super(context, attrs);
		initGameView();
	}

	private void initGameView() { // 初始化

		setColumnCount(4); // 指明GridLayout布局是4列的

		setBackgroundColor(0xffbbada0); // 配置GameView的背景或颜色

		setOnTouchListener(new View.OnTouchListener() { // 设置交互方式
														// 监听上下左右滑动的这几个动作，再由这几个动作去执行特定的代码，去实现游戏的逻辑

			private float startX, startY, offsetX, offsetY;

			@Override
			public boolean onTouch(View v, MotionEvent event) {

				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					startX = event.getX();
					startY = event.getY();
					break;
				case MotionEvent.ACTION_UP:
					offsetX = event.getX() - startX;
					offsetY = event.getY() - startY;

					if (Math.abs(offsetX) > Math.abs(offsetY)) { // 加此判断是为了解决当用户向斜方向滑动时程序应如何判断的问题

						if (offsetX < -5) {
							swipeLeft();
							// System.out.println("left");
						} else if (offsetX > 5) {
							swipeRight();
							// System.out.println("right");
						}

					} else { // 判断向上向下

						if (offsetY < -5) {
							swipeUp();
							// System.out.println("up");
						} else if (offsetY > 5) {
							swipeDown();
							// System.out.println("down");
						}

					}
					break;
				}

				return true; // 此处必须返回true,如返回false，则只会监听到MotionEvent.ACTION_DOWN这个事件，返回此事件没有成功，所以后面的事件也不会发生
			}
		});

	}

	// 只有第一次创建的时候才会执行一次 只可能会执行一次
	// 手机横放的时候不会执行，因为布局宽高不会发生改变，在AndroidManifest文件中配置了横放手机布局不变的参数
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) { // 动态计算卡片宽高
		super.onSizeChanged(w, h, oldw, oldh);

		int cardWidth = (Math.min(w, h) - 10) / 4;
		int cardHeight = cardWidth;

		addCards(cardWidth, cardHeight);

		startGame(); //这里开启游戏
						// 因为第一次创建游戏时，onSizeChanged()会被调用，且仅被调用一次，所以在这里开启游戏很合适

	}

	private void addCards(int cardWidth, int cardHeight) {

		Card card;

		for (int y = 0; y < 4; y++) {
			for (int x = 0; x < 4; x++) {
				card = new Card(getContext());
				card.setNum(0); // 刚开始全部添加0 此时并不会显示数字
				addView(card, cardWidth, cardHeight); // card是一个继承自FrameLayout的View
														// 在initGameView()中指明这个GridLayout是四列的方阵
				cardsMap[x][y] = card;
			}
		}
	}

	private void startGame() { // 开启游戏 若重新开始游戏的话就要先清理，清理完成后添加随机数

		MainActivity.getMainActivity().clearScore(); // 刚开始分数清零

		for (int y = 0; y < 4; y++) { // 对所有值进行清理
			for (int x = 0; x < 4; x++) {
				cardsMap[x][y].setNum(0);
			}
		}
		addRandomNum(); // 需要添加两次 两个随机数
		addRandomNum();
	}

	private void addRandomNum() { // 添加随机数 首先需要遍历所有卡片

		emptyPoints.clear(); // 添加随机数之前先清空emptyPoints,然后把每一个空点都添加进来

		for (int y = 0; y < 4; y++) {
			for (int x = 0; x < 4; x++) {

				if (cardsMap[x][y].getNum() <= 0) {
					emptyPoints.add(new Point(x, y)); // 把空点的位置添加进去
														// 因为只有空点才能够去添加数字
														// 已经有数字的话肯定不会去添加了
				}

			}
		}

		Point p = emptyPoints
				.remove((int) (Math.random() * emptyPoints.size())); // 随机地移除一个点
																		// 注意这里仅仅是移除emptyPoints中记录的点了，并没有移除card
		cardsMap[p.x][p.y].setNum(Math.random() > 0.1 ? 2 : 4); // 给这个空点添加一个数，2或4，概率为9：1

	}

	// 实现游戏逻辑 只要有位置的改变就添加新的 最重要的部分：游戏逻辑
	private void swipeLeft() {

		boolean merge = false; // 判断是否有合并，如果有的话就进行一些处理

		for (int y = 0; y < 4; y++) {
			for (int x = 0; x < 4; x++) {
				for (int x1 = x + 1; x1 < 4; x1++) { // 从当前的位置往右边去遍历

					if (cardsMap[x1][y].getNum() > 0) { // 如果往右去遍历得到的card的值（获取到的值）不是空的，则做如下逻辑判断

						if (cardsMap[x][y].getNum() <= 0) { // 如果当前位置上的值是空的，则将获取到的值移动到当前位置上
							cardsMap[x][y].setNum(cardsMap[x1][y].getNum());
							cardsMap[x1][y].setNum(0);

							x--; // 这里非常重要！！！！ 可以测试理解
							merge = true;
						} else if (cardsMap[x][y].equals(cardsMap[x1][y])) { // 如果当前位置上的值不是空的，而且获取到的值与当前位置上的值相等，则做相加处理，并将结果放在当前位置上
							cardsMap[x][y].setNum(cardsMap[x][y].getNum() * 2);
							cardsMap[x1][y].setNum(0);

							// 合并时加分 有合并就有添加（分数）
							MainActivity.getMainActivity().addScore(
									cardsMap[x][y].getNum());
							merge = true;
						}
						break; // 这个break的位置非常重要！！！！！ 只能写在这里！！ eg:方格最下面一行是2 32
								// 64 2，然后往左滑动的情况！
					}
				}
			}
		}

		if (merge) { // 在添加数字时判断游戏是否结束
			addRandomNum();
			checkComplete(); // 添加新项后都要检查游戏是否结束：没空位置，而且已经不能再合并
		}
	}

	private void swipeRight() {

		boolean merge = false;

		for (int y = 0; y < 4; y++) {
			for (int x = 3; x >= 0; x--) {
				for (int x1 = x - 1; x1 >= 0; x1--) {
					if (cardsMap[x1][y].getNum() > 0) { // 不是空的

						if (cardsMap[x][y].getNum() <= 0) {
							cardsMap[x][y].setNum(cardsMap[x1][y].getNum());
							cardsMap[x1][y].setNum(0);

							x++;

							merge = true;

						} else if (cardsMap[x][y].equals(cardsMap[x1][y])) {
							cardsMap[x][y].setNum(cardsMap[x][y].getNum() * 2);
							cardsMap[x1][y].setNum(0);

							// 合并时加分
							MainActivity.getMainActivity().addScore(
									cardsMap[x][y].getNum());

							merge = true;
						}

						break;

					}
				}
			}
		}

		if (merge) {
			addRandomNum();
			checkComplete();
		}

	}

	private void swipeUp() {

		boolean merge = false;

		for (int x = 0; x < 4; x++) {
			for (int y = 0; y < 4; y++) {
				for (int y1 = y + 1; y1 < 4; y1++) {

					if (cardsMap[x][y1].getNum() > 0) { // 不是空的

						if (cardsMap[x][y].getNum() <= 0) {
							cardsMap[x][y].setNum(cardsMap[x][y1].getNum());
							cardsMap[x][y1].setNum(0);

							y--;

							merge = true;

						} else if (cardsMap[x][y].equals(cardsMap[x][y1])) {
							cardsMap[x][y].setNum(cardsMap[x][y].getNum() * 2);
							cardsMap[x][y1].setNum(0);

							// 合并时加分
							MainActivity.getMainActivity().addScore(
									cardsMap[x][y].getNum());

							merge = true;

						}

						break;

					}
				}
			}
		}

		if (merge) {
			addRandomNum();
			checkComplete();
		}

	}

	private void swipeDown() {

		boolean merge = false;

		for (int x = 0; x < 4; x++) {
			for (int y = 3; y >= 0; y--) {
				for (int y1 = y - 1; y1 >= 0; y1--) {
					if (cardsMap[x][y1].getNum() > 0) { // 不是空的

						if (cardsMap[x][y].getNum() <= 0) {
							cardsMap[x][y].setNum(cardsMap[x][y1].getNum());
							cardsMap[x][y1].setNum(0);

							y++;

							merge = true;

						} else if (cardsMap[x][y].equals(cardsMap[x][y1])) {
							cardsMap[x][y].setNum(cardsMap[x][y].getNum() * 2);
							cardsMap[x][y1].setNum(0);

							// 合并时加分
							MainActivity.getMainActivity().addScore(
									cardsMap[x][y].getNum());

							merge = true;

						}

						break;

					}
				}
			}
		}

		if (merge) {
			addRandomNum();
			checkComplete();
		}

	}

	// 判断游戏是否结束的逻辑
	private void checkComplete() {

		boolean complete = true;

		All: for (int y = 0; y < 4; y++) {
			for (int x = 0; x < 4; x++) {

				// 游戏没有结束的判定情况 5种情况
				if (cardsMap[x][y].getNum() == 0
						|| (x > 0 && cardsMap[x][y].equals(cardsMap[x - 1][y]))
						|| (x < 3 && cardsMap[x][y].equals(cardsMap[x + 1][y]))
						|| (y > 0 && cardsMap[x][y].equals(cardsMap[x][y - 1]))
						|| (y < 3 && cardsMap[x][y].equals(cardsMap[x][y + 1]))) {

					complete = false;
					break All; // 写一个标签，跳出所有循环
				}
			}
		}

		if (complete) {
			AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
			dialog.setTitle("你好")
					.setMessage("游戏结束")
					.setPositiveButton("重来",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									startGame();
								}
							});
			
			dialog.setNegativeButton("关闭程序", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					MainActivity.getMainActivity().finish();
				}
			});
			dialog.show();
		}

	}

}
