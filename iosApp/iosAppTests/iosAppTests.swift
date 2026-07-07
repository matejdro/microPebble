import XCTest
@testable import iosApp

final class iosAppTests: XCTestCase {

    /// Smoke check that the app shell links; the shared UI is covered by iosAppUITests.
    func testAppShellLinks() {
        XCTAssertNotNil(SwiftDummy())
    }
}
