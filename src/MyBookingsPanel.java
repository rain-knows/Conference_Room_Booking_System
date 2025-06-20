import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutionException;

/**
 * 用户的预订面板，用于显示和管理用户的会议室预订。
 * 此面板将展示用户的预订列表，并提供查看、更新、取消等操作。
 */
public class MyBookingsPanel extends JPanel {

    private final User currentUser;
    private JTable bookingsTable;
    private DefaultTableModel tableModel;
    private final ReservationDAO reservationDAO;
    private final MeetingRoomDAO meetingRoomDAO; // For fetching room details on edit
    private List<Reservation> userReservations;

    private JButton editBookingButton;
    private JButton cancelBookingButton;

    /**
     * 构造函数，初始化用户的预订面板。
     * 设置布局，添加控件，并加载预订数据。
     */
    public MyBookingsPanel(User user) {
        this.currentUser = user;
        this.reservationDAO = new ReservationDAO();
        this.meetingRoomDAO = new MeetingRoomDAO();
        initComponents();
        loadBookings();
    }

    private void initComponents() {
        setLayout(new MigLayout("fill, insets 20", "[grow]", "[][grow][]"));

        // 标题
        JLabel titleLabel = new JLabel("我的预订记录");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 26));
        titleLabel.setForeground(new Color(41, 128, 185));
        add(titleLabel, "wrap, gapbottom 15");

        // 表格
        String[] columnNames = { "预订ID", "会议室", "会议主题", "开始时间", "结束时间", "状态" };
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // 表格内容不可编辑
            }
        };
        bookingsTable = new JTable(tableModel);
        bookingsTable.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        bookingsTable.setRowHeight(28);
        bookingsTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 16));
        bookingsTable.getTableHeader().setBackground(new Color(230, 235, 245));
        bookingsTable.setSelectionBackground(new Color(204, 229, 255));
        bookingsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bookingsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateButtonStates();
            }
        });

        JScrollPane scrollPane = new JScrollPane(bookingsTable);
        add(scrollPane, "grow, wrap, gapbottom 15");

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);
        editBookingButton = new JButton("修改选中的预订");
        cancelBookingButton = new JButton("取消选中的预订");
        JButton[] btns = { editBookingButton, cancelBookingButton };
        Color mainColor = new Color(52, 152, 219);
        Color hoverColor = new Color(41, 128, 185);
        for (JButton btn : btns) {
            btn.setBackground(mainColor);
            btn.setForeground(Color.WHITE);
            btn.setFont(new Font("微软雅黑", Font.BOLD, 15));
            btn.setFocusPainted(false);
            btn.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    btn.setBackground(hoverColor);
                }

                public void mouseExited(java.awt.event.MouseEvent evt) {
                    btn.setBackground(mainColor);
                }
            });
        }
        cancelBookingButton.setBackground(new Color(220, 53, 69));
        cancelBookingButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                cancelBookingButton.setBackground(new Color(200, 35, 51));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                cancelBookingButton.setBackground(new Color(220, 53, 69));
            }
        });
        editBookingButton.addActionListener(e -> editSelectedBooking());
        cancelBookingButton.addActionListener(e -> cancelSelectedBooking());
        buttonPanel.add(editBookingButton);
        buttonPanel.add(cancelBookingButton);
        add(buttonPanel, "growx, align right");

        setBackground(new Color(245, 248, 252));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(10, 10, 10, 10),
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true)));

        updateButtonStates(); // Initial state
    }

    /**
     * 加载用户的预订数据并更新UI。
     * 目前使用占位数据，后续应从数据库获取。
     */
    private void loadBookings() {
        new SwingWorker<List<Reservation>, Void>() {
            @Override
            protected List<Reservation> doInBackground() throws Exception {
                return reservationDAO.getReservationsByUserId(currentUser.getUserId());
            }

            @Override
            protected void done() {
                try {
                    userReservations = get();
                    updateTableModel(userReservations);
                    updateButtonStates();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(MyBookingsPanel.this, "加载预订记录失败: " + e.getMessage(), "错误",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void updateTableModel(List<Reservation> reservations) {
        tableModel.setRowCount(0); // 清空表格
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        for (Reservation res : reservations) {
            Vector<String> row = new Vector<>();
            row.add(String.valueOf(res.getReservationId()));
            row.add(res.getRoomName());
            row.add(res.getSubject());
            row.add(dateFormat.format(res.getStartTime()));
            row.add(dateFormat.format(res.getEndTime()));
            row.add(res.getStatusText());
            tableModel.addRow(row);
        }
    }

    private void updateButtonStates() {
        int selectedRow = bookingsTable.getSelectedRow();
        if (selectedRow == -1) {
            editBookingButton.setEnabled(false);
            cancelBookingButton.setEnabled(false);
        } else {
            Reservation selectedReservation = userReservations.get(selectedRow);
            boolean isConfirmed = selectedReservation.getStatus() == Reservation.STATUS_CONFIRMED;
            editBookingButton.setEnabled(isConfirmed);
            cancelBookingButton.setEnabled(isConfirmed);
        }
    }

    private Reservation getSelectedReservation() {
        int selectedRow = bookingsTable.getSelectedRow();
        if (selectedRow == -1 || userReservations == null || selectedRow >= userReservations.size()) {
            return null;
        }
        return userReservations.get(selectedRow);
    }

    private void editSelectedBooking() {
        Reservation reservation = getSelectedReservation();
        if (reservation == null) {
            JOptionPane.showMessageDialog(this, "请先选择一个您想修改的预订。", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // To open the dialog, we need the full MeetingRoom object.
        new SwingWorker<MeetingRoom, Void>() {
            @Override
            protected MeetingRoom doInBackground() throws Exception {
                return meetingRoomDAO.getMeetingRoomById(reservation.getRoomId());
            }

            @Override
            protected void done() {
                try {
                    MeetingRoom room = get();
                    if (room != null) {
                        ReservationDialog dialog = new ReservationDialog(
                                (Frame) SwingUtilities.getWindowAncestor(MyBookingsPanel.this),
                                currentUser, room, reservation, (saved) -> {
                                    if (saved) {
                                        loadBookings(); // Refresh list on successful save
                                    }
                                });
                        dialog.setVisible(true);
                    } else {
                        JOptionPane.showMessageDialog(MyBookingsPanel.this, "无法获取会议室信息，无法修改预订。", "错误",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(MyBookingsPanel.this, "打开修改窗口时出错: " + e.getMessage(), "错误",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void cancelSelectedBooking() {
        Reservation reservation = getSelectedReservation();
        if (reservation == null) {
            JOptionPane.showMessageDialog(this, "请先选择一个您想取消的预订。", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirmation = JOptionPane.showConfirmDialog(this, "您确定要取消这个预订吗？", "确认取消", JOptionPane.YES_NO_OPTION);
        if (confirmation != JOptionPane.YES_OPTION) {
            return;
        }

        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return reservationDAO.updateReservationStatus(reservation.getReservationId(),
                        Reservation.STATUS_CANCELLED);
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(MyBookingsPanel.this, "预订已成功取消。", "成功",
                                JOptionPane.INFORMATION_MESSAGE);
                        loadBookings();
                    } else {
                        JOptionPane.showMessageDialog(MyBookingsPanel.this, "取消预订失败。", "错误", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(MyBookingsPanel.this, "取消预订时发生错误: " + e.getMessage(), "错误",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }
}