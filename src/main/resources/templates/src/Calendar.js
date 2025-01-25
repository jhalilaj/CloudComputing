import React, { useState } from 'react';
import Calendar from 'react-calendar';
import 'react-calendar/dist/Calendar.css'; // Default CSS for the calendar

const AppointmentCalendar = () => {
  const [date, setDate] = useState(new Date()); // Selected date
  const [slots, setSlots] = useState([]); // Available slots for the selected date
  const [bookedSlot, setBookedSlot] = useState(null); // Track the booked slot

  // Mock data for available slots (7:00 - 23:00, 30-minute intervals)
  const generateSlots = (selectedDate) => {
    const slots = [];
    const startTime = new Date(selectedDate);
    startTime.setHours(7, 0, 0, 0); // Start at 07:00
    const endTime = new Date(selectedDate);
    endTime.setHours(23, 0, 0, 0); // End at 23:00

    while (startTime < endTime) {
      slots.push(new Date(startTime));
      startTime.setMinutes(startTime.getMinutes() + 30); // Add 30-minute intervals
    }

    return slots;
  };

  // Handle date change
  const handleDateChange = (newDate) => {
    setDate(newDate);
    setSlots(generateSlots(newDate)); // Generate slots for the selected date
  };

  // Handle slot booking
  const handleBookSlot = (slot) => {
    if (bookedSlot) {
      const confirmChange = window.confirm(
        `You already have a booking for ${bookedSlot.toLocaleTimeString()}. Do you want to change it to ${slot.toLocaleTimeString()}?`
      );
      if (confirmChange) {
        setBookedSlot(slot);
        alert(`Your booking has been changed to ${slot.toLocaleTimeString()}`);
      }
    } else {
      setBookedSlot(slot);
      alert(`Slot booked for ${slot.toLocaleTimeString()}`);
    }
  };

  // Handle slot cancellation
  const handleCancelSlot = () => {
    setBookedSlot(null);
    alert('Your booking has been canceled.');
  };

  return (
    <div style={{ display: 'flex', justifyContent: 'center', marginTop: '20px' }}>
      <div>
        <h2>Select a Date</h2>
        <Calendar onChange={handleDateChange} value={date} />
      </div>
      <div style={{ marginLeft: '20px', maxWidth: '600px' }}>
        <h2>Available Slots</h2>
        <div
          style={{
            display: 'flex',
            flexWrap: 'wrap',
            gap: '10px', // Space between slots
          }}
        >
          {slots.length > 0 ? (
            slots.map((slot, index) => (
              <div
                key={index}
                style={{
                  padding: '10px',
                  border: '1px solid #ccc',
                  borderRadius: '4px',
                  cursor: 'pointer',
                  backgroundColor: bookedSlot && bookedSlot.getTime() === slot.getTime() ? '#ffcccc' : '#f4f4f4',
                  minWidth: '100px', // Fixed width for each slot
                  textAlign: 'center',
                }}
                onClick={() => handleBookSlot(slot)}
              >
                {slot.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
              </div>
            ))
          ) : (
            <p>No slots available for this date.</p>
          )}
        </div>

        {/* Display booked slot and cancel button */}
        {bookedSlot && (
          <div style={{ marginTop: '20px' }}>
            <h3>Your Booked Slot</h3>
            <p>{bookedSlot.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}</p>
            <button
              onClick={handleCancelSlot}
              style={{
                padding: '10px',
                backgroundColor: '#dc3545',
                color: '#fff',
                border: 'none',
                borderRadius: '4px',
                cursor: 'pointer',
              }}
            >
              Cancel Booking
            </button>
          </div>
        )}
      </div>
    </div>
  );
};

export default AppointmentCalendar;