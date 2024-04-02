var countries = {
    "auto": "Auto (recommended)",
    "nl": "Netherlands",
    "de": "Germany",
    "us": "United States",
    "pl": "Poland",
    "at": "Austria",
    "lu": "Luxembourg",
    "fr": "France",
    "no": "Norway",
    "ro": "Romania",
    "se": "Sweden",
    "dk": "Denmark",
    "ca": "Canada",
    "sw": "Switzerland",
    "ru": "Russia",
    "uk": "United Kingdom",
};

// Remove the "auto" option from the countries object
var autoOption = { auto: countries.auto };
delete countries.auto;

// Convert the countries object into an array of key-value pairs
var countriesArray = Object.entries(countries);

// Sort the array by the country names
countriesArray.sort(function(a, b) {
    return a[1].localeCompare(b[1]);
});

// Convert the sorted array back into an object
var sortedCountries = Object.fromEntries(countriesArray);

// Add the "auto" option back to the top of the sorted countries object
countries = { ...autoOption, ...sortedCountries };