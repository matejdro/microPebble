import UIKit
import Shared

class SceneDelegate: UIResponder, UIWindowSceneDelegate {
    var window: UIWindow?

    func scene(
        _ scene: UIScene,
        willConnectTo session: UISceneSession,
        options connectionOptions: UIScene.ConnectionOptions
    ) {
        guard let windowScene = (scene as? UIWindowScene) else { return }
        
        let window = UIWindow(windowScene: windowScene)

        let rootViewController = MainViewControllerKt.MainViewController()
        
        window.rootViewController = rootViewController
        window.makeKeyAndVisible()
        
        self.window = window
    }
}

