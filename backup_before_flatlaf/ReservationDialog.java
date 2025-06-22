import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.function.Consumer;

public class ReservationDialog extends JDialog {

    private final ReservationDAO reservationDAO;
    private final Consumer<Boolean> onSaveCallback;
    private final User currentUser;
    private final MeetingRoom room;
    private Reservation currentReservation; // null for new reservation

    private JTextField subjectField;
    private JTextArea descriptionArea;
    private JSpinner startDateSpinner;
    private JSpinner startTimeSpinner;
    private JSpinner endDateSpinner;
    private JSpinner endTimeSpinner;
    private boolean saved = false;

    public ReservationDialog(Frame owner, User currentUser, MeetingRoom room, Reservation reservation,
            Consumer<Boolean> onSaveCallback) {
        super(owner, true);
        this.currentUser = currentUser;
        this.room = room;
        this.currentReservation = reservation;
        this.reservationDAO = new ReservationDAO();
        this.onSaveCallback = onSaveCallback;

        initComponents();
        populateData();
    }

    private void initComponents() {
        setTitle(currentReservation == null ? "新建预订 - " + room.getName() : "修改预订 - " + room.getName());
        setLayout(new MigLayout("fill, insets 25", "[120px][grow,fill][grow,fill][grow,fill]", "[][][][][grow][]"));

        // 设置对话框样式
        getContentPane().setBackground(new Color(248, 249, 250));

        // 标题区域
        JLabel titleLabel = new JLabel(currentReservation == null ? "新建预订" : "修改预订");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 20));
        titleLabel.setForeground(new Color(52, 73, 94));
        add(titleLabel, "span 4, center, wrap, gapbottom 20");

        // 会议室信息
        JLabel roomInfoLabel = new JLabel("会议室: " + room.getName() + " (" + room.getLocation() + ")");
        roomInfoLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        roomInfoLabel.setForeground(new Color(108, 117, 125));
        add(roomInfoLabel, "span 4, center, wrap, gapbottom 15");

        // Components with better styling
        subjectField = new JTextField();
        subjectField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        subjectField.setPreferredSize(new Dimension(300, 30));

        descriptionArea = new JTextArea(5, 30);
        descriptionArea.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(206, 212, 218), 1),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));

        // Date and Time Spinners with better styling
        startDateSpinner = new JSpinner(new SpinnerDateModel());
        startDateSpinner.setEditor(new JSpinner.DateEditor(startDateSpinner, "yyyy-MM-dd"));
        startDateSpinner.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        startDateSpinner.setPreferredSize(new Dimension(120, 30));

        startTimeSpinner = new JSpinner(new SpinnerDateModel(new Date(), null, null, Calendar.MINUTE));
        startTimeSpinner.setEditor(new JSpinner.DateEditor(startTimeSpinner, "HH:mm"));
        startTimeSpinner.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        startTimeSpinner.setPreferredSize(new Dimension(80, 30));

        endDateSpinner = new JSpinner(new SpinnerDateModel());
        endDateSpinner.setEditor(new JSpinner.DateEditor(endDateSpinner, "yyyy-MM-dd"));
        endDateSpinner.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        endDateSpinner.setPreferredSize(new Dimension(120, 30));

        endTimeSpinner = new JSpinner(new SpinnerDateModel(new Date(), null, null, Calendar.MINUTE));
        endTimeSpinner.setEditor(new JSpinner.DateEditor(endTimeSpinner, "HH:mm"));
        endTimeSpinner.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        endTimeSpinner.setPreferredSize(new Dimension(80, 30));

        // Layout with better spacing and styling
        JLabel subjectLabel = new JLabel("会议主题:");
        subjectLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        subjectLabel.setForeground(new Color(52, 73, 94));
        add(subjectLabel);
        add(subjectField, "span 3, growx, wrap, gaptop 5, gapbottom 15");

        JLabel startLabel = new JLabel("开始时间:");
        startLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        startLabel.setForeground(new Color(52, 73, 94));
        add(startLabel);
        add(startDateSpinner, "growx");
        add(startTimeSpinner, "span 2, growx, wrap, gaptop 5, gapbottom 15");

        JLabel endLabel = new JLabel("结束时间:");
        endLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        endLabel.setForeground(new Color(52, 73, 94));
        add(endLabel);
        add(endDateSpinner, "growx");
        add(endTimeSpinner, "span 2, growx, wrap, gaptop 5, gapbottom 15");

        JLabel descLabel = new JLabel("会议描述:");
        descLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        descLabel.setForeground(new Color(52, 73, 94));
        add(descLabel, "top, gaptop 5");
        add(new JScrollPane(descriptionArea), "span 3, grow, gaptop 5, gapbottom 20");

        // Buttons with better styling
        JButton saveButton = new JButton("保存预订");
        saveButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        saveButton.setBackground(new Color(40, 167, 69));
        saveButton.setForeground(Color.WHITE);
        saveButton.setPreferredSize(new Dimension(100, 35));
        saveButton.setFocusPainted(false);
        saveButton.addActionListener(e -> saveReservation());

        JButton cancelButton = new JButton("取消");
        cancelButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        cancelButton.setBackground(new Color(108, 117, 125));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setPreferredSize(new Dimension(100, 35));
        cancelButton.setFocusPainted(false);
        cancelButton.addActionListener(e -> dispose());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, "span 4, center, gaptop 10");

        pack();
        setMinimumSize(new Dimension(600, 450));
        setResizable(true);
        setLocationRelativeTo(getOwner());
    }

    private void populateData() {
        if (currentReservation != null) {
            Date start = new Date(currentReservation.getStartTime().getTime());
            Date end = new Date(currentReservation.getEndTime().getTime());
            subjectField.setText(currentReservation.getSubject());
            descriptionArea.setText(currentReservation.getDescription());
            startDateSpinner.setValue(start);
            startTimeSpinner.setValue(start);
            endDateSpinner.setValue(end);
            endTimeSpinner.setValue(end);
        } else {
            // Default for new reservation: start from next hour, duration 1 hour
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.HOUR_OF_DAY, 1);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            Date startTime = cal.getTime();
            cal.add(Calendar.HOUR_OF_DAY, 1);
            Date endTime = cal.getTime();

            startDateSpinner.setValue(startTime);
            startTimeSpinner.setValue(startTime);
            endDateSpinner.setValue(endTime);
            endTimeSpinner.setValue(endTime);
        }
    }

    private Timestamp getTimestampFromSpinners(JSpinner dateSpinner, JSpinner timeSpinner) {
        Calendar dateCal = Calendar.getInstance();
        dateCal.setTime((Date) dateSpinner.getValue());

        Calendar timeCal = Calendar.getInstance();
        timeCal.setTime((Date) timeSpinner.getValue());

        dateCal.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY));
        dateCal.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE));
        dateCal.set(Calendar.SECOND, 0);
        dateCal.set(Calendar.MILLISECOND, 0);

        return new Timestamp(dateCal.getTimeInMillis());
    }

    private void saveReservation() {
        Timestamp startTime = getTimestampFromSpinners(startDateSpinner, startTimeSpinner);
        Timestamp endTime = getTimestampFromSpinners(endDateSpinner, endTimeSpinner);

        // Validation
        if (subjectField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "会议主题不能为空。", "输入错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (startTime.after(endTime) || startTime.equals(endTime)) {
            JOptionPane.showMessageDialog(this, "结束时间必须晚于开始时间。", "输入错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Collect data
        String subject = subjectField.getText().trim();
        String description = descriptionArea.getText().trim();

        // Prepare reservation object
        Reservation reservationToSave;
        if (currentReservation == null) { // Create new
            reservationToSave = new Reservation(0, currentUser.getUserId(), room.getRoomId(), room.getName(), subject,
                    description, startTime, endTime, Reservation.STATUS_CONFIRMED);
        } else { // Update existing
            reservationToSave = new Reservation(currentReservation.getReservationId(), currentUser.getUserId(),
                    room.getRoomId(), room.getName(), subject, description, startTime, endTime,
                    currentReservation.getStatus());
        }

        // Use SwingWorker for database operation
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                if (reservationToSave.getReservationId() == 0) { // New
                    return reservationDAO.createReservation(reservationToSave);
                } else { // Update
                    return reservationDAO.updateReservation(reservationToSave);
                }
            }

            @Override
            protected void done() {
                try {
                    boolean success = get();
                    if (success) {
                        saved = true;
                        JOptionPane.showMessageDialog(ReservationDialog.this, "预订保存成功！", "成功",
                                JOptionPane.INFORMATION_MESSAGE);
                        dispose();
                    }
                } catch (Exception e) {
                    Throwable cause = e.getCause();
                    String errorMessage = cause != null ? cause.getMessage() : e.getMessage();
                    JOptionPane.showMessageDialog(ReservationDialog.this, "保存失败: " + errorMessage, "数据库错误",
                            JOptionPane.ERROR_MESSAGE);
                } finally {
                    if (onSaveCallback != null) {
                        onSaveCallback.accept(saved);
                    }
                }
            }
        }.execute();
    }
}