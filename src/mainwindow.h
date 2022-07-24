#ifndef MAINWINDOW_H
#define MAINWINDOW_H

#include <QtNetwork/QtNetwork>
#include <QtWidgets/QMainWindow>

namespace Ui {
class MainWindow;
}

class MainWindow : public QMainWindow {
  Q_OBJECT
public:
  MainWindow(QWidget *parent = 0);
  ~MainWindow();

protected:
  void changeEvent(QEvent *e);

private:
  Ui::MainWindow *ui;
  QByteArray magic_packet;
  bool started;
  QUdpSocket *udpSocket;
  QSettings *settings;

  void GenerateMagicPacket(QString);
  QString processMAC_addr(QString str);
  QHostAddress processSpecialAddr();
  void restoreWindowSettings();
  void saveWindowSettings();
  void readProfileNames();
  void writeProfileValues();
  void ui_updateProfileSettings(QString);
  void ui_refreshProfileNames();
  void sendMagicPacket(QAbstractSocket::SocketType type, QHostAddress addr);

private slots:
  void on_startButton_clicked();
  void on_profileNameComboBox_currentIndexChanged(int index);
  void on_profileSaveButton_clicked();
  void on_profileDeleteButton_clicked();
  void on_sendButton_clicked();
  void host_looked_up(const QHostInfo &host);
  void onUdpSocketReadyRead();
};

#endif // MAINWINDOW_H
