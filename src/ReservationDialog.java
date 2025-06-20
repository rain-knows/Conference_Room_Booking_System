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
        setLayout(new MigLayout("wrap 4, fillx, insets 20", "[100px][grow,fill][grow,fill][grow,fill]"));

        // Components
        subjectField = new JTextField();
        descriptionArea = new JTextArea(4, 20);

        // Date and Time Spinners
        startDateSpinner = new JSpinner(new SpinnerDateModel());
        startDateSpinner.setEditor(new JSpinner.DateEditor(startDateSpinner, "yyyy-MM-dd"));
        startTimeSpinner = new JSpinner(new SpinnerDateModel(new Date(), null, null, Calendar.MINUTE));
        startTimeSpinner.setEditor(new JSpinner.DateEditor(startTimeSpinner, "HH:mm"));

        endDateSpinner = new JSpinner(new SpinnerDateModel());
        endDateSpinner.setEditor(new JSpinner.DateEditor(endDateSpinner, "yyyy-MM-dd"));
        endTimeSpinner = new JSpinner(new SpinnerDateModel(new Date(), null, null, Calendar.MINUTE));
        endTimeSpinner.setEditor(new JSpinner.DateEditor(endTimeSpinner, "HH:mm"));

        // Layout
        add(new JLabel("会议主题:"));
        add(subjectField, "span 3, growx");

        add(new JLabel("开始时间:"));
        add(startDateSpinner, "growx");
        add(startTimeSpinner, "span 2, growx");

        add(new JLabel("结束时间:"));
        add(endDateSpinner, "growx");
        add(endTimeSpinner, "span 2, growx");

        add(new JLabel("会议描述:"), "top, gaptop 5");
        add(new JScrollPane(descriptionArea), "span 3, grow, gaptop 5");

        // Buttons
        JButton saveButton = new JButton("保存");
        saveButton.addActionListener(e -> saveReservation());
        JButton cancelButton = new JButton("取消");
        cancelButton.addActionListener(e -> dispose());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, "span 4, growx, gaptop 10");

        pack();
        setMinimumSize(new Dimension(550, 350));
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