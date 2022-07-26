#include "mainwindow.h"

#include <QSettings>
#include <QtWidgets/QMessageBox>

#include "ui_mainwindow.h"

MainWindow::MainWindow(QWidget *parent)
    : QMainWindow(parent), ui(new Ui::MainWindow), started(false),
      udpSocket(new QUdpSocket(this)),
      settings(new QSettings("settings.ini", QSettings::IniFormat)) {
  ui->setupUi(this);
  restoreWindowSettings();
  readProfileNames();
  connect(ui->profileNameComboBox, SIGNAL(currentIndexChanged(int)), this,
          SLOT(on_profileNameComboBox_currentIndexChanged(int)));
  connect(udpSocket, SIGNAL(readyRead()), this, SLOT(onUdpSocketReadyRead()));
}

MainWindow::~MainWindow() {
  saveWindowSettings();
  delete settings;
  delete ui;
}

void MainWindow::changeEvent(QEvent *e) {
  QMainWindow::changeEvent(e);
  switch (e->type()) {
  case QEvent::LanguageChange:
    ui->retranslateUi(this);
    break;
  default:
    break;
  }
}

void MainWindow::on_sendButton_clicked() {
  QHostInfo::lookupHost(ui->IP_lineEdit->text(), this,
                        SLOT(host_looked_up(QHostInfo)));
}

void MainWindow::GenerateMagicPacket(QString str) {
  for (int i = 0; i != 6; i++) {
    magic_packet.append(0xFF);
  }

  QByteArray hex = QByteArray::fromHex(str.toLatin1());
  for (int i = 0; i != 16; i++) {
    magic_packet.append(hex);
  }
}

QString MainWindow::processMAC_addr(QString str) {
  if (str.indexOf(":") != -1) {
    int pos = 0;
    for (int i = 0; i != str.length(); i++) {
      pos = str.indexOf(":", pos);
      if (pos == -1)
        break;
      str.remove(pos, 1);
    }
  }
  return str;
}

void MainWindow::restoreWindowSettings() {
  settings->beginGroup("MainWindow");

  QByteArray geometry = settings->value("geometry").toByteArray();
  if (!geometry.isEmpty()) {
    restoreGeometry(geometry);
  }

  QByteArray state = settings->value("state").toByteArray();
  if (!state.isEmpty()) {
    restoreState(state);
  }

  settings->endGroup();
}

void MainWindow::saveWindowSettings() {
  settings->beginGroup("MainWindow");

  settings->setValue("geometry", saveGeometry());
  settings->setValue("windowState", saveState());

  settings->endGroup();
}

void MainWindow::readProfileNames() {
  settings->beginGroup("Profiles");

  QStringList profiles = settings->childGroups();

  for (QString &profileName : profiles) {
    ui->profileNameComboBox->addItem(profileName);
  }

  settings->endGroup();

  ui_updateProfileSettings(ui->profileNameComboBox->currentText());
}

void MainWindow::ui_updateProfileSettings(QString currentProfile) {
  settings->beginGroup("Profiles");
  settings->beginGroup(currentProfile);

  QString ip = settings->value("ip").toString();
  ui->IP_lineEdit->setText(ip);

  QString mac = settings->value("MAC").toString();
  ui->MAC_lineEdit->setText(mac);

  QString port = settings->value("port").toString();
  if (!port.isEmpty()) {
    ui->portLineEdit->setText(port);
  } else {
    ui->portLineEdit->setText("9");
  }

  settings->endGroup();
  settings->endGroup();
}

void MainWindow::writeProfileValues() {
  QString section_name = ui->profileNameComboBox->currentText();
  settings->beginGroup("Profiles");
  settings->beginGroup(section_name);

  settings->setValue("ip", ui->IP_lineEdit->text());
  settings->setValue("MAC", ui->MAC_lineEdit->text());
  settings->setValue("port", ui->portLineEdit->text());

  settings->endGroup();
  settings->endGroup();
}

void MainWindow::on_profileSaveButton_clicked() {
  writeProfileValues();
  QString current_text = ui->profileNameComboBox->lineEdit()->text();
  ui_refreshProfileNames();
  int current_index = ui->profileNameComboBox->findText(current_text);
  ui->profileNameComboBox->setCurrentIndex(current_index);
}

void MainWindow::on_profileDeleteButton_clicked() {
  settings->beginGroup("Profiles");
  QString profileToDelete = ui->profileNameComboBox->currentText();
  settings->remove(profileToDelete);
  settings->endGroup();

  ui_refreshProfileNames();
}

void MainWindow::on_profileNameComboBox_currentIndexChanged(int index) {
  ui_updateProfileSettings(ui->profileNameComboBox->currentText());
}

void MainWindow::host_looked_up(const QHostInfo &host) {
  if (host.error() != QHostInfo::NoError) {
    QMessageBox(QMessageBox::Warning, "ERROR!", host.errorString()).exec();
    return;
  }
  QHostAddress ipv4_addr;
  for (QHostAddress &addr : host.addresses()) {
    if (addr.protocol() == QAbstractSocket::IPv4Protocol) {
      ipv4_addr = addr;
    }
  }
  QString MAC = processMAC_addr(ui->MAC_lineEdit->text());
  GenerateMagicPacket(MAC);
  QUdpSocket UdpSocket(this);
  int udpReturn = UdpSocket.writeDatagram(magic_packet, ipv4_addr,
                                          ui->portLineEdit->text().toUShort());
  if (udpReturn == -1)
    QMessageBox(QMessageBox::Warning, "ERROR!", UdpSocket.errorString()).exec();
  magic_packet.clear();
}

void MainWindow::ui_refreshProfileNames() {
  ui->profileNameComboBox->clear();
  readProfileNames();
}

void MainWindow::on_startButton_clicked() {
  if (started == false) {
    if (!udpSocket->bind(ui->receivePortLineEdit->text().toUShort())) {
      QMessageBox(QMessageBox::Warning, "ERROR!", udpSocket->errorString())
          .exec();
      return;
    }
    ui->startButton->setText("Stop");
    started = true;
  } else {
    ui->startButton->setText("Start");
    udpSocket->close();
    started = false;
  }
}

void MainWindow::onUdpSocketReadyRead() {
  int dataSize = udpSocket->pendingDatagramSize();
  char *rawData = new char[dataSize];
  QHostAddress addr;
  quint16 port;
  udpSocket->readDatagram(rawData, dataSize, &addr, &port);
  QByteArray data = QByteArray::fromRawData(rawData, dataSize);
  QByteArray hexedData = data.toHex();
  for (int i = 2; i != hexedData.size(); i = i + 3) {
    hexedData.insert(i, " ");
  }
  QString str;
  QTextStream(&str) << "Packet received from " << addr.toString() << " on port "
                    << port << ", packet length: " << dataSize << "\n"
                    << hexedData << "\n";
  ui->packetInfoTextEdit->appendPlainText(str);
  delete rawData;
}
