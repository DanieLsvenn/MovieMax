# ProfileActivity Implementation

This implementation provides a comprehensive user profile system for the MovieMax application with the following features:

## Features

### 1. **User Profile Tab**

- Displays comprehensive user information including:
  - Profile picture placeholder
  - Full name and email
  - Personal information (gender, phone, date of birth)
  - Account information (username, role, member since date)
- Real-time data loading from the API
- Error handling with user-friendly messages
- Loading indicators for better UX

### 2. **My Tickets Tab**

- Displays all user's purchased tickets
- Shows ticket details:
  - Movie title and cinema information
  - Room name and showtime
  - Seat selections
  - Total amount paid
  - Booking status with color coding
- Pull-to-refresh functionality
- Empty state when no tickets exist
- Click handling to view detailed ticket information

### 3. **Navigation Integration**

- Added to the sidebar menu in DashboardActivity
- Accessible via the "👤 Profile" option
- Clean navigation flow with proper back button handling

## API Integration

The ProfileActivity uses the following API endpoints:

### 1. Get Account Information

```
GET /api/accounts/{id}
```

Retrieves user profile information including personal details and account settings.

### 2. Get User Bookings

```
GET /api/accounts/{accountsId}/bookings
```

Fetches all tickets/bookings associated with the user account.

### 3. Update Account (Ready for future use)

```
PUT /api/accounts/{id}
```

For future profile editing functionality.

## Usage

### Accessing the Profile

1. Open the app and navigate to any activity with the sidebar menu
2. Tap the hamburger menu (☰) to open the sidebar
3. Select "👤 Profile" from the menu
4. The ProfileActivity will open with two tabs

### Profile Tab

- Automatically loads and displays user information
- Shows loading indicator while fetching data
- Displays error messages if data fails to load
- All information is formatted for easy reading

### Tickets Tab

- Automatically loads user's booking history
- Shows empty state if no tickets exist
- Pull down to refresh the ticket list
- Tap any ticket to view detailed information
- Color-coded status indicators:
  - Green: Confirmed/Active tickets
  - Red: Cancelled tickets
  - Orange: Pending tickets
  - Gray: Other statuses

## Technical Implementation

### Key Components

1. **ProfileActivity** - Main activity with TabLayout and ViewPager2
2. **UserInfoFragment** - Handles user profile display
3. **TicketsFragment** - Manages ticket list and interactions
4. **BookingAdapter** - RecyclerView adapter for ticket items
5. **ProfilePagerAdapter** - ViewPager2 adapter for tab management

### Models Used

- **AccountResponse** - User account information
- **BookingResponse** - Ticket/booking information with nested FoodItem class

### Key Features

- Responsive layout with CardViews
- Material Design components
- Pull-to-refresh functionality
- Error handling and loading states
- Session management integration
- Clean architecture with separation of concerns

## Future Enhancements

1. **Profile Editing**

   - Implement edit profile functionality
   - Photo upload capability
   - Password change option

2. **Ticket Actions**

   - Cancel ticket functionality
   - Download/share ticket as image or PDF
   - QR code generation for cinema entry

3. **Enhanced UI**
   - Profile picture upload and display
   - Dark mode support
   - Animation improvements

## Files Created/Modified

### New Files:

- `ProfileActivity.java`
- `UserInfoFragment.java`
- `TicketsFragment.java`
- `BookingAdapter.java`
- `ProfilePagerAdapter.java`
- `AccountResponse.java`
- Various layout files and drawables

### Modified Files:

- `ApiService.java` - Added new API endpoints
- `BookingResponse.java` - Added bookingDate field
- `SidebarFragment.java` - Added Profile navigation
- `DashboardActivity.java` - Added Profile navigation handling
- `AndroidManifest.xml` - Registered ProfileActivity
- `colors.xml` - Added new color resources

This implementation provides a solid foundation for user profile management and ticket viewing, with clean architecture that allows for easy future enhancements.
