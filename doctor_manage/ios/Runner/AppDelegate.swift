import UIKit
import Flutter
import AVFoundation

@UIApplicationMain
@objc class AppDelegate: FlutterAppDelegate {
  private let audioChannel = "ai_scribe_copilot/audio"
  private let dndChannel = "ai_scribe_copilot/dnd"
  private let interruptionChannel = "ai_scribe_copilot/interruption"

  override func application(
    _ application: UIApplication,
    didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
  ) -> Bool {
    let controller : FlutterViewController = window?.rootViewController as! FlutterViewController

    let audioMethodChannel = FlutterMethodChannel(name: audioChannel, binaryMessenger: controller.binaryMessenger)
    audioMethodChannel.setMethodCallHandler { (call, result) in
      switch call.method {
      case "startBackgroundService":
        // TODO: Enable background audio mode
        result(true)
      case "stopBackgroundService":
        // TODO: Disable background audio mode
        result(true)
      case "switchAudioRoute":
        // TODO: Switch audio route (Bluetooth/wired)
        result(true)
      default:
        result(FlutterMethodNotImplemented)
      }
    }

    let dndMethodChannel = FlutterMethodChannel(name: dndChannel, binaryMessenger: controller.binaryMessenger)
    dndMethodChannel.setMethodCallHandler { (call, result) in
      if call.method == "isDoNotDisturbEnabled" {
        // TODO: Check DND status (iOS does not provide a public API for this; may need to always return false)
        result(false)
      } else {
        result(FlutterMethodNotImplemented)
      }
    }

    // TODO: Listen for phone call/interruption events and send to Flutter via interruptionChannel

    GeneratedPluginRegistrant.register(with: self)
    return super.application(application, didFinishLaunchingWithOptions: launchOptions)
  }
}