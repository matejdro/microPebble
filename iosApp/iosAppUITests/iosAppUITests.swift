import XCTest

final class iosAppUITests: XCTestCase {

    override func setUpWithError() throws {
        continueAfterFailure = false
    }

    /// Verifies the shared home screen renders and the hand-rolled tab switching works on iOS.
    @MainActor
    func testTabSwitching() throws {
        let app = XCUIApplication()
        app.launch()

        // Starts on the Watches tab.
        XCTAssertTrue(app.staticTexts["WATCHES"].waitForExistence(timeout: 15), "Home did not render")

        // Tap the Tools tab (4th of 4 items in the bottom bar).
        app.coordinate(withNormalizedOffset: CGVector(dx: 0.875, dy: 0.96)).tap()
        XCTAssertTrue(app.staticTexts["TOOLS"].waitForExistence(timeout: 5), "Did not switch to Tools")

        // Tap back to the Watches tab (1st item).
        app.coordinate(withNormalizedOffset: CGVector(dx: 0.125, dy: 0.96)).tap()
        XCTAssertTrue(app.staticTexts["WATCHES"].waitForExistence(timeout: 5), "Did not switch back to Watches")
    }
}
